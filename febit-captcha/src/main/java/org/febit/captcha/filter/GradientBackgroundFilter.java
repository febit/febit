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
package org.febit.captcha.filter;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.febit.captcha.Filter;

public class GradientBackgroundFilter implements Filter {

    protected Color colorFrom = Color.LIGHT_GRAY;
    protected Color colorTo = Color.WHITE;

    @Override
    public BufferedImage render(final BufferedImage baseImage) {

        int width = baseImage.getWidth();
        int height = baseImage.getHeight();

        // create an opaque image
        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));

        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));

        BufferedImage imageWithBackground = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graph = (Graphics2D) imageWithBackground.getGraphics();
        graph.setRenderingHints(hints);
        graph.setPaint(new GradientPaint(0, 0, colorFrom, width, height, colorTo));
        graph.fill(new Rectangle2D.Double(0, 0, width, height));
        graph.drawImage(baseImage, 0, 0, null);
        graph.dispose();

        return imageWithBackground;
    }
}
