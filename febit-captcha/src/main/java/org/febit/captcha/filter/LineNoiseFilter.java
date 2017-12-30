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
import org.febit.util.RandomUtil;

/**
 *
 * * @author zqq90
 */
public class LineNoiseFilter implements Filter {

    protected int count = 3;

    @Override
    public BufferedImage render(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final Graphics graph = image.getGraphics();
        for (int i = 0, len = count; i < len; i++) {
            graph.setColor(new Color(RandomUtil.nextInt(), true));
            graph.drawLine(RandomUtil.nextInt(width),
                    RandomUtil.nextInt(height),
                    RandomUtil.nextInt(width),
                    RandomUtil.nextInt(height));
        }
        return image;
    }
}
