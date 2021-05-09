package me.mateus.milkshake.core.utils;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static byte[] bufferedImageToBytes(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            byte[] array = os.toByteArray();
            os.close();
            return array;
        } catch (IOException e) {
            return null;
        }
    }
}
