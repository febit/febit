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
package org.febit.captcha;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 *
 * * @author zqq90
 */
public class Captcha {

    protected TextProducer textProducer;
    protected WordRender wordRenderer;
    protected Filter[] filters = null;
    protected int charLength = 6;
    protected int width = 200;
    protected int height = 50;
    protected String imageFormat = "jpg";

    public BufferedImage createImage(String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        wordRenderer.render(image, text);
        final Filter[] filters = this.filters;
        if (filters != null) {
            for (Filter filter : filters) {
                image = filter.render(image);
            }
        }
        return image;
    }

    public String createText() {
        return textProducer.createText();
    }

    public void write(String text, OutputStream out) throws IOException {
        ImageIO.write(
                createImage(text == null ? createText() : text),
                imageFormat,
                out);
    }

}
