package com.flamespeed;

import com.flamespeed.video.*;


public class FlameSpeed {
    public static void main(String[] args) {
        try {
            VideoFile v = new CineFile("c5h12_air_T353_10atm_phi110_ppcm942_sg050_s120_5k_aug20_2008.cine");
        } catch (VideoLoadException e) {
            System.out.println(e);
        }
    }
}