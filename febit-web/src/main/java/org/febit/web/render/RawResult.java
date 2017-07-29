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
package org.febit.web.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import jodd.io.StreamUtil;
import org.febit.web.ActionRequest;
import org.febit.web.util.ServletUtil;

/**
 *
 * @author zqq90
 */
public class RawResult implements Renderable {

    protected final InputStream inputStream;
    protected final String downloadFileName;
    protected final String mimeType;
    protected final String etag;
    protected final int length;

    protected RawResult(InputStream inputStream, String downloadFileName, String mimeType, String etag, int length) {
        this.inputStream = inputStream;
        this.downloadFileName = downloadFileName;
        this.mimeType = mimeType;
        this.etag = etag;
        this.length = length;
    }

    public RawResult(String downloadFileName, File file, String mimeType, String etag) {
        this(createFileInputStream(file), downloadFileName, mimeType, etag, (int) file.length());
    }

    public RawResult(String downloadFileName, File file, String mimeType) {
        this(createFileInputStream(file), downloadFileName, mimeType, null, (int) file.length());
    }

    public RawResult(File file, String mimeType, String etag) {
        this(createFileInputStream(file), file.getName(), mimeType, etag, (int) file.length());
    }

    public RawResult(File file, String mimeType) {
        this(createFileInputStream(file), file.getName(), mimeType, null, (int) file.length());
    }

    public RawResult(File file) {
        this(file, null, null);
    }

    private static FileInputStream createFileInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object render(ActionRequest actionRequest) throws IOException {
        HttpServletResponse response = actionRequest.response;
        if (etag != null) {
            response.setHeader("Etag", etag);
        }
        ServletUtil.prepareResponse(response, downloadFileName, mimeType, length);

        InputStream contentInputStream = this.inputStream;
        OutputStream out = response.getOutputStream();

        StreamUtil.copy(contentInputStream, out);

        out.flush();

        StreamUtil.close(contentInputStream);
        return null;
    }
}
