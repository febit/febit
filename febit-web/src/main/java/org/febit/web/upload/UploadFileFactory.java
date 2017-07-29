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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jodd.io.FastByteArrayOutputStream;
import jodd.io.FileUtil;
import jodd.io.StreamUtil;

/**
 *
 * @author zqq90
 */
public class UploadFileFactory {

    protected static final String TMP_FILE_PREFIX = "febit-upload-";

    protected int memoryThreshold = 8192;
    protected int maxFileSize = 1024000;

    public UploadFile create(final MultipartRequestInputStream input) throws IOException {
        final MultipartRequestHeader header = input.lastHeader;

        byte[] data = null;

        if (memoryThreshold > 0) {
            FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream(memoryThreshold + 1);
            int written = input.copyMax(fbaos, memoryThreshold + 1);
            data = fbaos.toByteArray();
            if (written <= memoryThreshold) {
                return new UploadFile(header, true, data.length, false, null, data);
            }
        }

        final File tempFile = FileUtil.createTempFile(TMP_FILE_PREFIX, null, null);
        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
        int size = 0;

        //push data
        if (data != null) {
            size = data.length;
            out.write(data);
            data = null;
        }

        boolean deleteTempFile = false;
        try {
            if (maxFileSize == -1) {
                size += input.copyAll(out);
            } else {
                size += input.copyMax(out, maxFileSize - size + 1);  // one more byte to detect larger files
                if (size > maxFileSize) {
                    deleteTempFile = true;
                    input.skipToBoundary();
                    return new UploadFile(header, false, size, true, null, null);
                }
            }
            return new UploadFile(header, true, size, false, tempFile, null);
        } finally {
            StreamUtil.close(out);
            if (deleteTempFile) {
                tempFile.delete();
            }
        }
    }
}
