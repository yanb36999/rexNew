package com.zmcsoft.rex.utils.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhouhao
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageLayer implements Layer {
    private InputStream imageInput;

    private int x;

    private int y;

    private int width;

    private int height;

    @Override
    public void draw(Graphics graphics) {
        BufferedImage layerImage;
        try {
            layerImage = ImageIO.read(getImageInput());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        graphics.drawImage(layerImage,
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                (img, infoflags, x, y, width, height) -> true);
    }
}
