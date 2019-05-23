package com.flamespeed.video;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class CineFile extends VideoFile {
    private int frameSize;
    private long[] frameOffsets; // Offsets of each frame
    private FileInputStream fileStream; // The ID of the opened file

    public CineFile(String fileName) throws VideoLoadException {
        this.fileName = fileName;

        // The code here is based on the partial specification provided at the
        // link below as well as on previous c/c++ implementations of this
        // https://wiki.multimedia.cx/index.php?title=Phantom_Cine
        // This python implementation also proved useful
        // https://github.com/adamdlight/pyCine/blob/master/pycine.py
        try {
            fileStream = new FileInputStream(fileName);
            FileChannel channel = fileStream.getChannel();
            
            // Read the .cine file header into a byte buffer
            int amountToRead = 44;
            ByteBuffer cineFileHeader = ByteBuffer.allocate(amountToRead);
            // The only way to read the cine file structs properly.
            // The data is stored with least significant bytes first.
            cineFileHeader.order(ByteOrder.LITTLE_ENDIAN);
            int amountRead = channel.read(cineFileHeader);
            
            if (amountRead < amountToRead) {
                throw new VideoLoadException("Failed to read file");
            }

            // Check that this is in fact a CINE file
            if (cineFileHeader.getShort(0) != (('I' << 8) | 'C')) {
                throw new VideoLoadException("Bad File Format");
            }
            // Check the version of this CINE file (version 1)
            if (cineFileHeader.getShort(6) != 1) {
                throw new VideoLoadException("This file is not CINE version 1");
            }
            // Check the compression format (0 corresponds to RGB)
            if (cineFileHeader.getShort(4) != 0) {
                throw new VideoLoadException("This file's compression is not RGB");
            }
            
            // Read the locaion of the bitmap header
            int offsetBitmapHeader = cineFileHeader.getInt(24);

            // Read the bitmap header into a byte buffer
            // Struct has the format of a standard BitmapInfoHeader
            amountToRead = 40;
            ByteBuffer bitmapInfoHeader = ByteBuffer.allocate(amountToRead);
            bitmapInfoHeader.order(ByteOrder.LITTLE_ENDIAN);
            amountRead = channel.read(bitmapInfoHeader, offsetBitmapHeader);

            if (amountRead < amountToRead) {
                throw new VideoLoadException("Failed to read file");
            }
            
            // Check that the images are 8-bit
            if (bitmapInfoHeader.getShort(14) != 8) {
                throw new VideoLoadException("This file is not 8-bits per pixel");
            }

            numFrames = cineFileHeader.getInt(20);
            width = bitmapInfoHeader.getInt(4);
            height = bitmapInfoHeader.getInt(8);
            frameSize = width * height;

            // Read the location of the frame offsets
            int offsetFrameOffsets = cineFileHeader.getInt(32);

            // Read the positions of each frame from the file
            amountToRead = numFrames*8;
            ByteBuffer byteOffsets = ByteBuffer.allocate(amountToRead);
            byteOffsets.order(ByteOrder.LITTLE_ENDIAN);
            amountRead = channel.read(byteOffsets, offsetFrameOffsets);

            if (amountRead < amountToRead) {
                throw new VideoLoadException("Failed to read file");
            }

            // Store the image offsets as an array
            byteOffsets.rewind();
            frameOffsets = new long[numFrames];
            for (int i = 0; i < frameOffsets.length; i++) {
                frameOffsets[i] = byteOffsets.getLong();
            }
        } catch (IOException e) {
            throw new VideoLoadException("Failed to read file");
        }
    }

    
    public BufferedImage getFrame(int i) throws FrameLoadException {
        try {
            // Generate a file channel from the filestream
            FileChannel channel = fileStream.getChannel();
            // Get the offset for frame i from the stored array
            long offset = frameOffsets[i];

            // Read the 4 byte header size from the file
            int amountToRead = 4;
            ByteBuffer header = ByteBuffer.allocate(amountToRead);
            header.order(ByteOrder.LITTLE_ENDIAN);
            int amountRead = channel.read(header, offset);

            if (amountRead < amountToRead) {
                throw new FrameLoadException("Failed to load frame: "+i);
            }

            // Read the size of the header
            int headerSize = header.getInt(0);

            // Move past the header and read the image into a buffer
            ByteBuffer byteImage = ByteBuffer.allocate(frameSize);
            byteImage.order(ByteOrder.LITTLE_ENDIAN);
            amountRead = channel.read(byteImage, offset+headerSize);

            if (amountRead < frameSize) {
                throw new FrameLoadException("Failed to load frame: "+i);
            }

            // Access the underlying array of the buffered image
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            final byte[] arr = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

            // Copy contents into the underlying array
            System.arraycopy(byteImage.array(), 0, arr, 0, frameSize);

            return image;
        } catch(IOException e) {
            throw new FrameLoadException("Failed to load frame: "+i);
        }
    }


    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}