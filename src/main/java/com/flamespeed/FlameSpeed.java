package com.flamespeed;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import com.flamespeed.display.BasicDisplay;
import com.flamespeed.video.*;


public class FlameSpeed {
    public static void main(String[] args) {
        try {
            VideoFile v = new CineFile("c5h12_air_T353_10atm_phi110_ppcm942_sg050_s120_5k_aug20_2008.cine");

            BasicDisplay disp = new BasicDisplay();

            for (int i = 0; i < v.getNumFrames(); i++) {
                BufferedImage img = v.get_frame(i);
                disp.display(img);
                TimeUnit.MILLISECONDS.sleep(41);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}