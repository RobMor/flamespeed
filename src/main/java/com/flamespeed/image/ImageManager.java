package com.flamespeed.image;

public class ImageManager {
    private byte[] imageArray; /** The array containing the raw image data */
    private int width; /** Width in pixels of the image */
    private int height; /** Height in pixels of the image */
    private int channels; /** Number of channels in each pixel of the image */

    /**
     * Construct an RGB image from a byte array.
     * 
     * @param imageArray An array of bytes representing the entire image.
     * @param width The width in pixels of the image.
     * @param height The height in pixels of the image.
     */
    public ImageManager(byte[] imageArray, int width, int height) {
        this.imageArray = imageArray;
        this.width = width; // Width in pixels
        this.height = height;
        this.channels = 3;
    }

    /**
     * Construct an image from a byte array using something other than three
     * channels.
     * 
     * @param imageArray An array of bytes representing the entire image.
     * @param width The width in pixels of the image.
     * @param height The height in pixels of the image.
     * @param channels The number of channels in the image.
     */
    // public ImageManager(byte[] imageArray, int width, int height, ChannelType channels) {
    //     this.imageArray = imageArray;
    //     this.width = width; // Width in pixels
    //     this.height = height;
    //     switch (channels) {
    //         case RGB:
    //             this.channels = 3; break;
    //         case GRAY:
    //             this.channels = 1; break;
    //     }
    // }

    // Implement support for setting pixels RGB values
    /**
     * Set the pixel to the given value. Only works for grayscale images. Sets
     * each channel of the pixel to the provided value.
     * 
     * @param x The x or column coordinate of the pixel to change.
     * @param y The y or row coordinate of the pixel to change.
     * @param v The value to set the pixel to.
     * @return True if the operation was successful, false otherwise.
     */
    public boolean set(int x, int y, byte v) {
        if (x < width && y < height) {
            int row = y;
            int col = channels*x;
            int index = (channels*width)*row + col;

            for (int i = 0; i < channels; i++)
                imageArray[index+i] = v;

            return true;
        } else {
            return false;
        }
    }
}