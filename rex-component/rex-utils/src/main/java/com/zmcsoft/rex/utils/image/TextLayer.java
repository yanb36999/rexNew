package com.zmcsoft.rex.utils.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextLayer implements Layer {
    private int x;

    private int y;

    private Font font;

    private String text;

    private Color color;

    @Override
    public void draw(Graphics graphics) {
        Font temp = graphics.getFont();
        Color tempColor = graphics.getColor();
        if (font != null) {
            graphics.setFont(font);
        }
        if (color != null) {
            graphics.setColor(color);
        }
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawString(text, x, y);
        graphics.setFont(temp);
        graphics.setColor(tempColor);
    }
}
