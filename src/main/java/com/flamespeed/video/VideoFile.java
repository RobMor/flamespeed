package com.flamespeed.video;

import com.flamespeed.image.Image;
import com.flamespeed.video.CineFile;
import com.flamespeed.video.VideoLoadException;

public abstract class VideoFile {
    protected String fileName;
    protected int num_frames; // Number of frames in the video
    protected int frame_px_width; // Width in pixels
    protected int frame_px_height; // Height in pixels
    protected int frame_line_size; // TODO remove this
    protected int frame_size; // Size of the frame in bytes
    protected long frame_rate; // Frame rate of the video



    public static VideoFile loadVideo(String fileName) throws VideoLoadException {
        return new CineFile("testing.cine");
    }

    public abstract Image get_frame(int i) throws FrameLoadException;
}