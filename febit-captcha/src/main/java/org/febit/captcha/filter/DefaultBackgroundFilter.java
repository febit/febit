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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.febit.captcha.Filter;

public class DefaultBackgroundFilter implements Filter {

    protected Color color = Color.WHITE;

    @Override
    public BufferedImage render(final BufferedImage image) {

        final int width = image.getWidth();
        final int height = image.getHeight();

        final BufferedImage imageWithBackground = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        final Graphics graph = imageWithBackground.getGraphics();
        graph.setColor(color);
        graph.fillRect(0, 0, width, height);
        graph.drawImage(image, 0, 0, null);
        graph.dispose();

        return imageWithBackground;
    }
}
