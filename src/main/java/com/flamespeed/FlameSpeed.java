package com.flamespeed;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import com.flamespeed.display.BasicDisplay;
import com.flamespeed.video.*;

public class FlameSpeed {
    public static void main(String[] args) {
        try {
            VideoFile v = new OtherFile("c5h12_air_T353_10atm_phi110_ppcm942_sg050_s120_5k_aug20_2008.cine");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}