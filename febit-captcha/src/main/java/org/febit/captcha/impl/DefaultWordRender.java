/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.captcha.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.febit.captcha.WordRender;
import org.febit.util.RandomUtil;

public class DefaultWordRender implements WordRender {

    //settings
    protected Font[] fonts = {new Font(Font.SERIF, Font.BOLD, 20)};
    protected Color[] colors = {Color.BLUE, Color.DARK_GRAY};
    protected int charSpace = 5;

    @Override
    public void render(BufferedImage image, String word) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        Graphics2D graphics2D = image.createGraphics();

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        graphics2D.setRenderingHints(hints);

        char[] wordChars = word.toCharArray();
        int wordLen = wordChars.length;

        Font[] chosenFonts = new Font[wordLen];
        int[] charWidths = new int[wordLen];
        int widthNeeded = 0;

        int maxFontSize = 0;

        for (int i = 0; i < wordLen; i++) {
            Font font = RandomUtil.rnd(fonts);

            if (font.getSize() > maxFontSize) {
                maxFontSize = font.getSize();
            }

            FontMetrics fontMetrics = graphics2D.getFontMetrics(font);
            int charWidth = fontMetrics.charWidth(wordChars[i]);
            widthNeeded = widthNeeded + charWidth;
            chosenFonts[i] = font;
            charWidths[i] = charWidth;
        }
        widthNeeded += charSpace * (wordLen - 1);

        int startPosX = (width - widthNeeded) / 2;
        int startPosY = (height - maxFontSize) / 5 + maxFontSize;

        for (int i = 0; i < wordLen; i++) {
            graphics2D.setFont(chosenFonts[i]);
            graphics2D.setColor(RandomUtil.rnd(colors));
            graphics2D.drawString(String.valueOf(wordChars[i]), startPosX, startPosY);
            startPosX = startPosX + charWidths[i] + charSpace;
        }
    }
}
