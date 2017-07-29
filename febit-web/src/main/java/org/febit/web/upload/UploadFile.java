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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jodd.io.FileUtil;

public class UploadFile {

    public final String fieldName;
    public final String fileName;
    public final String contentType;

    public final boolean valid;
    public final int size;
    public final boolean fileTooBig;

    protected final File tempFile;
    protected final byte[] data;

    public UploadFile(MultipartRequestHeader header, boolean valid, int size, boolean fileTooBig, File tempFile, byte[] data) {

        this.fieldName = header.fieldName;
        this.fileName = header.fileName;
        this.contentType = header.contentType;

        this.valid = valid;
        this.size = size;
        this.fileTooBig = fileTooBig;
        this.tempFile = tempFile;
        this.data = data;
    }

    public boolean isInMemory() {
        return data != null;
    }

    /**
     * Deletes file uploaded item from disk or memory.
     */
    public void delete() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    /**
     * Writes file upload item to destination file.
     */
    public File write(String destination) throws IOException {
        return write(new File(destination));
    }

    /**
     * Writes file upload item to destination file.
     */
    public File write(File destination) throws IOException {
        if (data != null) {
            FileUtil.writeBytes(destination, data);
        } else if (tempFile != null) {
            FileUtil.move(tempFile, destination);
        }
        return destination;
    }

    /**
     * Returns the content of file upload item.
     */
    public byte[] getFileContent() throws IOException {
        if (data != null) {
            return data;
        }
        if (tempFile != null) {
            return FileUtil.readBytes(tempFile);
        }
        return null;
    }

    public InputStream getInputStream() throws IOException {
        if (data != null) {
            return new ByteArrayInputStream(data);
        }
        if (tempFile != null) {
            return new BufferedInputStream(new FileInputStream(tempFile));
        }
        return null;
    }
}
