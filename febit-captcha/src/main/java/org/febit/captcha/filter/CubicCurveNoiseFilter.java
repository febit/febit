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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import org.febit.captcha.Filter;
import org.febit.util.RandomUtil;

/**
 * Draws a noise on the image.
 *
 * The noise curve depends on the factor values. Noise won't be visible if all factors have the value > 1.0f
 */
public class CubicCurveNoiseFilter implements Filter {

    //settings
    protected Color color = Color.BLACK;
    protected float factor1 = 0.1f;
    protected float factor2 = 0.3f;
    protected float factor3 = 0.5f;
    protected float factor4 = 0.9f;

    @Override
    public BufferedImage render(BufferedImage image) {

        final int width = image.getWidth();
        final int height = image.getHeight();

        final PathIterator pi = new CubicCurve2D.Float(factor1 * width, height
                * RandomUtil.nextFloat(), factor2 * width, height
                * RandomUtil.nextFloat(), factor3 * width, height
                * RandomUtil.nextFloat(), factor4 * width, height
                * RandomUtil.nextFloat())
                .getPathIterator(null, 2);

        final int[] pts = new int[200];
        int i = 0;

        float[] coords = new float[6];
        while (!pi.isDone() && i < 200) {
            int seg = pi.currentSegment(coords);
            if (seg == PathIterator.SEG_MOVETO || seg == PathIterator.SEG_LINETO) {
                pts[i++] = (int) coords[0];
                pts[i++] = (int) coords[1];
            }
            pi.next();
        }

        final Graphics2D graph = (Graphics2D) image.getGraphics();
        graph.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));

        graph.setColor(color);

        i -= 2;
        int j = 0;
        while (j < i) {
            graph.drawLine(pts[j++], pts[j++], pts[j], pts[j + 1]);
        }

        graph.dispose();
        return image;
    }
}
