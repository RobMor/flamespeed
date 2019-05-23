package com.flamespeed.video;

import java.awt.image.BufferedImage;

import com.flamespeed.video.CineFile;
import com.flamespeed.video.VideoLoadException;

public abstract class VideoFile {
    protected String fileName;
    protected int numFrames; // Number of frames in the video
    protected int width; // Width in pixels
    protected int height; // Height in pixels

    public static VideoFile loadVideo(String fileName) throws VideoLoadException {
        // return new CineFile("testing.cine");
        // return new OtherFile("testing.cine");
        return null;
    }

    public String getFileName() {
        return fileName;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract BufferedImage getFrame(int i) throws FrameLoadException;
}