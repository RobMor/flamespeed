package com.flamespeed.video;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.flamespeed.image.Image;

public class CineFile extends VideoFile {
    private long[] offsets; // Offsets of each frame
    private FileInputStream fileStream; // The ID of the opened file

    public CineFile(String fileName) throws VideoLoadException {
        super(fileName);

        // The code here is based on the partial specification provided at the
        // link below as well as on previous c/c++ implementations of this
        // https://wiki.multimedia.cx/index.php?title=Phantom_Cine
        try {
            fileStream = new FileInputStream(fileName);
            FileChannel channel = fileStream.getChannel();
            
            // Read the .cine file header into a byte buffer
            ByteBuffer cineFileHeader = ByteBuffer.allocate(44);
            // The only way to read the cine file structs properly.
            // The data is stored with least significant bytes first.
            cineFileHeader.order(ByteOrder.LITTLE_ENDIAN);
            int amountRead = channel.read(cineFileHeader);
            
            if (amountRead < 44) {
                throw new VideoLoadException("Failed to read file");
            }

            System.out.println(bytesToHex(cineFileHeader.array()));
            // Check that this is in fact a CINE file
            if (cineFileHeader.getShort(0) != (('I' << 8) | 'C')) {
            //if ('I' != header.getChar(0) && 'C' != header.getChar(1)) {
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
            int offsetImageHeader = cineFileHeader.getInt(24);

            // Read the bitmap header into a byte buffer
            ByteBuffer bitmapInfoHeader = ByteBuffer.allocate(40);
            bitmapInfoHeader.order(ByteOrder.LITTLE_ENDIAN);
            amountRead = channel.read(bitmapInfoHeader, offsetImageHeader);

            if (amountRead < 40) {
                throw new VideoLoadException("Failed to read file");
            }
            
            // TODO problem here.
            // Check that the images are 8-bit
            System.out.println(bitmapInfoHeader.getShort(14));
            if (bitmapInfoHeader.getShort(14) != 8) {
                throw new VideoLoadException("This file is not 8-bits per pixel");
            }
            
            // Read the location of the setup information
            int offsetSetupInfo = cineFileHeader.getInt(26);

            // Read the setup information into a byte buffer
            ByteBuffer setupInfo = ByteBuffer.allocate(5692); // Biggest byte buffer yet...
            setupInfo.order(ByteOrder.LITTLE_ENDIAN);
            amountRead = channel.read(setupInfo, offsetSetupInfo);

            if (amountRead < 5692) {
                throw new VideoLoadException("Failed to read file");
            }
            
            // Check the setup mark
            System.out.println(bytesToHex(new byte[] {setupInfo.get(140), setupInfo.get(141)}));
            if (setupInfo.getShort(140) != ('S' | ('T' << 8))) {
                // Errors here generally mean that the file is either corrupt or
                // the position (140) is wrong. First try experimenting with
                // other values... This is the most mysterious of the structs...
                throw new VideoLoadException("This file has a bad setup mark");
            }

            num_frames = cineFileHeader.getInt(18);
            frame_px_width = bitmapInfoHeader.getInt(2);
            frame_px_height = bitmapInfoHeader.getInt(6);
            frame_line_size = frame_px_width*3;
            frame_size = bitmapInfoHeader.getInt(18)*3;
            frame_rate = 0;

        } catch (IOException e) {
            System.out.println("Error");
        }
    }

    public Image get_frame(int i) {
        return new Image();
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