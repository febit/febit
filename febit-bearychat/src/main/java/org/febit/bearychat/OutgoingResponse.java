/**
 * Copyright 2013 febit.org (support@febit.org)
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
package org.febit.bearychat;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import jodd.json.JsonSerializer;
import jodd.util.MimeTypes;
import org.febit.util.StringUtil;
import org.febit.web.ActionRequest;
import org.febit.web.render.Renderable;

/**
 *
 * @author zqq90
 */
public class OutgoingResponse implements Renderable {

    private static final JsonSerializer SERIALIZER;

    static {
        SERIALIZER = new JsonSerializer()
                .deep(true);
    }

    public static OutgoingResponse createMessage() {
        return new OutgoingResponse();
    }

    public static OutgoingResponse createMessage(String text) {
        return createMessage().setText(text);
    }

    public static OutgoingResponse createMessage(String text, Object... params) {
        return createMessage().setText(StringUtil.format(text, params));
    }

    public static Attachment createAttachment(String title, String text) {
        return new Attachment()
                .setTitle(title)
                .setText(text);
    }

    public static Attachment createAttachment(String title, String text, String color) {
        return new Attachment()
                .setTitle(title)
                .setText(text)
                .setColor(color);
    }

    public static Attachment createAttachment(String title, String text, String color, String image) {
        return new Attachment()
                .setTitle(title)
                .setText(text)
                .setColor(color)
                .addImages(image);
    }

    public static Attachment createAttachment(String title, String text, String color, String[] images) {
        return new Attachment()
                .setTitle(title)
                .setText(text)
                .setColor(color)
                .addImages(images);
    }

    public static OutgoingResponse errorResponse(String text, Object... params) {
        return OutgoingResponse.createMessage().addAttachment(null, StringUtil.format(text, params), "#e60000");
    }

    public static OutgoingResponse infoResponse(String text, Object... params) {
        return OutgoingResponse.createMessage().addAttachment(null, StringUtil.format(text, params), "#92d877");
    }

    public static OutgoingResponse warnResponse(String text, Object... params) {
        return OutgoingResponse.createMessage().addAttachment(null, StringUtil.format(text, params), "#ffbb00");
    }

    protected String text;
    protected List<Attachment> attachments;

    public OutgoingResponse addAttachment(String title, String text) {
        return addAttachments(createAttachment(title, text));
    }

    public OutgoingResponse addAttachment(String title, String text, String color) {
        return addAttachments(createAttachment(title, text, color));
    }

    public OutgoingResponse addAttachment(String title, String text, String color, String image) {
        return addAttachments(createAttachment(title, text, color, image));
    }

    public OutgoingResponse addAttachment(String title, String text, String color, String[] images) {
        return addAttachments(createAttachment(title, text, color, images));
    }

    public OutgoingResponse addAttachments(Attachment... attachments) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.addAll(Arrays.asList(attachments));
        return this;
    }

    public OutgoingResponse setText(String text) {
        this.text = text;
        return this;
    }

    public OutgoingResponse setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public Object render(ActionRequest actionRequest) throws Exception {

        final HttpServletResponse response = actionRequest.response;
        final String encoding = response.getCharacterEncoding();
        response.setContentType(MimeTypes.MIME_APPLICATION_JSON);
        response.setCharacterEncoding(encoding);

        final Writer out = response.getWriter();
        try {
            SERIALIZER.serialize(this, out);
        } finally {
            out.flush();
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public static class Attachment {

        protected String title;
        protected String text;
        protected String color;
        protected List<Image> images;

        public Attachment addImages(String... urls) {
            if (urls == null || urls.length == 0) {
                return this;
            }
            Image[] imgs = new Image[urls.length];
            for (int i = 0; i < urls.length; i++) {
                imgs[i] = new Image(urls[i]);
            }
            return addImages(imgs);
        }

        public Attachment addImages(Image... images) {
            if (this.images == null) {
                this.images = new ArrayList<>();
            }
            this.images.addAll(Arrays.asList(images));
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Attachment setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getText() {
            return text;
        }

        public Attachment setText(String text) {
            this.text = text;
            return this;
        }

        public String getColor() {
            return color;
        }

        public Attachment setColor(String color) {
            this.color = color;
            return this;
        }

        public List<Image> getImages() {
            return images;
        }

        public Attachment setImages(List<Image> images) {
            this.images = images;
            return this;
        }
    }

    public static class Image {

        protected String url;

        public Image() {
        }

        public Image(String url) {
            this.url = url;
        }
    }
}
