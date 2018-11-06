package com.flamespeed.video;

public class VideoLoadException extends Exception {
    VideoLoadException(){};

    VideoLoadException(String reason) {
        super(reason);
    }
}