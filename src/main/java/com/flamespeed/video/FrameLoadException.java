package com.flamespeed.video;

public class FrameLoadException extends Exception {
    FrameLoadException(){};

    FrameLoadException(String reason) {
        super(reason);
    }
}