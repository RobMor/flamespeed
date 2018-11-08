package com.flamespeed.display;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class BasicDisplay {
    JFrame frame;
    JLabel label;
    public BasicDisplay() {
        frame = new JFrame();
        label = new JLabel(new ImageIcon());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(label);
    }

    public void display(BufferedImage image) {
        label.setIcon(new ImageIcon(image));
        frame.pack();
        frame.setVisible(true);
        System.out.println("Updated");
    }
}