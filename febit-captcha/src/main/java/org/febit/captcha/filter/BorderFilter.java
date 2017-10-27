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

/**
 *
 * * @author zqq90
 */
public class BorderFilter implements Filter {

    protected Color borderColor = Color.BLACK;

    @Override
    public BufferedImage render(BufferedImage image) {

        final int x2 = image.getWidth() - 1;
        final int y2 = image.getHeight() - 1;

        Graphics graphics = image.createGraphics();
        graphics.setColor(borderColor);

        graphics.drawLine(0, 0, x2, 0);
        graphics.drawLine(0, 1, 0, y2);
        graphics.drawLine(1, y2, x2, y2);
        graphics.drawLine(x2, 1, x2, y2 - 1);

        return image;
    }
}
