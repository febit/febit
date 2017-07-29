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
package org.febit.web.upload;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import jodd.io.FastByteArrayOutputStream;
import jodd.util.ArraysUtil;
import org.febit.util.StringUtil;

public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    protected UploadFileFactory fileUploadFactory;
    protected Map<String, String[]> requestParameters = new HashMap<>();
    protected Map<String, UploadFile[]> requestFiles;
    protected HttpServletRequest request;

    private boolean parsed;

    public MultipartRequestWrapper(HttpServletRequest request, UploadFileFactory fileUploadFactory) {
        super(request);
        this.request = request;
        this.fileUploadFactory = fileUploadFactory;
    }

    protected void putFile(String name, UploadFile value) {
        if (requestFiles == null) {
            requestFiles = new HashMap<>();
        }

        UploadFile[] fileUploads = requestFiles.get(name);

        if (fileUploads != null) {
            fileUploads = ArraysUtil.append(fileUploads, value);
        } else {
            fileUploads = new UploadFile[]{value};
        }

        requestFiles.put(name, fileUploads);
    }

    protected void putParameter(String name, String value) {

        String[] params = requestParameters.get(name);

        if (params != null) {
            params = ArraysUtil.append(params, value);
        } else {
            params = new String[]{value};
        }

        requestParameters.put(name, params);
    }

    public void parseRequestStream() throws IOException {
        parseRequestStream("ISO-8859-1");
    }

    public void parseRequestStream(String encoding) throws IOException {
        if (parsed == true) {
            throw new IOException("Multi-part request already parsed");
        }
        parsed = true;

        final HttpServletRequest request = this.request;

        //parse queryString
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            for (String raw : StringUtil.splitc(queryString, '&')) {
                int split = raw.indexOf('=');
                if (split >= 0) {
                    putParameter(decodeUrl(raw.substring(0, split)).trim(),
                            decodeUrl(raw.substring(split + 1)).trim());
                } else {
                    putParameter(decodeUrl(raw).trim(), "");
                }
            }
        }

        final MultipartRequestInputStream input = new MultipartRequestInputStream(request.getInputStream());
        input.readBoundary();
        while (true) {
            MultipartRequestHeader header = input.readDataHeader(encoding);
            if (header == null) {
                break;
            }

            if (header.isFile == true) {
                String fileName = header.fileName;
                if (fileName.length() > 0) {
                    if (header.contentType.indexOf("application/x-macbinary") > 0) {
                        input.skipBytes(128);
                    }
                }
                UploadFile newFile = fileUploadFactory.create(input);
                putFile(header.fieldName, newFile);
            } else {
                // no file, therefore it is regular form parameter.
                FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
                input.copyAll(fbos);
                String value = encoding != null ? new String(fbos.toByteArray(), encoding) : new String(fbos.toByteArray());
                putParameter(header.fieldName, value);
            }

            input.skipBytes(1);
            input.mark(1);

            // read byte, but may be end of stream
            int nextByte = input.read();
            if (nextByte == -1 || nextByte == '-') {
                input.reset();
                break;
            }
            input.reset();
        }
    }

    @Override
    public String getParameter(String paramName) {
        String[] values = requestParameters.get(paramName);
        if (values != null && values.length != 0) {
            return values[0];
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(requestParameters.keySet());
    }

    @Override
    public String[] getParameterValues(String paramName) {
        return requestParameters.get(paramName);
    }

    public UploadFile getFile(String paramName) {
        if (requestFiles == null) {
            return null;
        }
        UploadFile[] values = requestFiles.get(paramName);
        if (values != null && values.length != 0) {
            return values[0];
        }
        return null;
    }

    public UploadFile[] getFiles(String paramName) {
        if (requestFiles == null) {
            return null;
        }
        return requestFiles.get(paramName);
    }

    public Set<String> getFileParameterNames() {
        if (requestFiles == null) {
            return Collections.emptySet();
        }
        return requestFiles.keySet();
    }

    private static String decodeUrl(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, "UTF8");
    }
}
