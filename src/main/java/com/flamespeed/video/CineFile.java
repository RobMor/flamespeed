package com.flamespeed.video;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

import com.flamespeed.image.*;

public class CineFile extends VideoFile {
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

            num_frames = cineFileHeader.getInt(20);
            frame_px_width = bitmapInfoHeader.getInt(4);
            frame_px_height = bitmapInfoHeader.getInt(8);
            frame_line_size = frame_px_width*3;
            frame_size = frame_line_size * frame_px_height;

            // Read the location of the frame offsets
            int offsetFrameOffsets = cineFileHeader.getInt();

            // Read the positions of each frame from the file
            amountToRead = num_frames*8;
            ByteBuffer byteOffsets = ByteBuffer.allocate(amountToRead);
            byteOffsets.order(ByteOrder.LITTLE_ENDIAN);
            amountRead = channel.read(byteOffsets, offsetFrameOffsets);

            if (amountRead < amountToRead) {
                throw new VideoLoadException("Failed to read file");
            }

            // Store the image offsets as an array
            byteOffsets.rewind();
            frameOffsets = byteOffsets.asLongBuffer().array();
        } catch (IOException e) {
            throw new VideoLoadException("Failed to read file");
        }
    }

    
    public Image get_frame(int i) throws FrameLoadException {
        try {
            FileChannel channel = fileStream.getChannel();
            long offset = frameOffsets[i];

            ByteBuffer imageBuffer = ByteBuffer.allocate(frame_size);
            int amountRead = channel.read(imageBuffer, offset);

            if (amountRead < frame_size) {
                throw new FrameLoadException("Failed to load frame: "+i);
            }

            return new Image(imageBuffer.array(), frame_px_width, frame_px_height, ChannelType.RGB);
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