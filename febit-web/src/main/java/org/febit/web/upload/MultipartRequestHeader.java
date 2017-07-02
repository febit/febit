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
package org.febit.web.upload;

public class MultipartRequestHeader {

    public final String dataHeader;

    public final String fieldName;
    public final String fileName;

    public final boolean isFile;
    public final String contentType;

    MultipartRequestHeader(String dataHeader) {
        this.dataHeader = dataHeader;
        this.fieldName = getDataFieldValue(dataHeader, "name");
        this.fileName = getDataFieldValue(dataHeader, "filename");
        this.isFile = fileName != null && !fileName.isEmpty();
        if (isFile) {
            int pos = dataHeader.indexOf("Content-Type:");
            if (pos == -1) {
                this.contentType = "";
            } else {
                this.contentType = dataHeader.substring(pos + 13);
            }
        } else {
            this.contentType = null;
        }
    }

    private static String getDataFieldValue(String dataHeader, String fieldName) {
        String token = fieldName + "=\"";
        int pos = dataHeader.indexOf(token);
        if (pos > 0) {
            int start = pos + token.length();
            int end = dataHeader.indexOf('"', start);
            if (end > 0) {
                return dataHeader.substring(start, end);
            }
        }
        return null;
    }
}
