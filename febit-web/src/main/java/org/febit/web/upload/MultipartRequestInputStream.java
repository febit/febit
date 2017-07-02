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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jodd.io.FastByteArrayOutputStream;

public class MultipartRequestInputStream extends BufferedInputStream {

    protected byte[] boundary;
    protected MultipartRequestHeader lastHeader;

    public MultipartRequestInputStream(InputStream in) {
        super(in);
    }

    public byte readByte() throws IOException {
        int i = super.read();
        if (i == -1) {
            throw new IOException("End of HTTP request stream reached");
        }
        return (byte) i;
    }

    public void skipBytes(int i) throws IOException {
        long len = super.skip(i);
        if (len != i) {
            throw new IOException("Failed to skip data in HTTP request");
        }
    }

    public byte[] readBoundary() throws IOException {
        FastByteArrayOutputStream boundaryOutput = new FastByteArrayOutputStream();
        byte b;
        // skip optional whitespaces
        while ((b = readByte()) <= ' ') {
        }
        boundaryOutput.write(b);

        // now read boundary chars
        while ((b = readByte()) != '\r') {
            boundaryOutput.write(b);
        }
        if (boundaryOutput.size() == 0) {
            throw new IOException("Problems with parsing request: invalid boundary");
        }
        skipBytes(1);
        boundary = new byte[boundaryOutput.size() + 2];
        System.arraycopy(boundaryOutput.toByteArray(), 0, boundary, 2, boundary.length - 2);
        boundary[0] = '\r';
        boundary[1] = '\n';
        return boundary;
    }

    public MultipartRequestHeader readDataHeader(String encoding) throws IOException {
        String dataHeader = readDataHeaderString(encoding);
        if (dataHeader != null) {
            return lastHeader = new MultipartRequestHeader(dataHeader);
        } else {
            return lastHeader = null;
        }
    }

    protected String readDataHeaderString(String encoding) throws IOException {
        FastByteArrayOutputStream data = new FastByteArrayOutputStream();
        byte b;
        while (true) {
            // end marker byte on offset +0 and +2 must be 13
            if ((b = readByte()) != '\r') {
                data.write(b);
                continue;
            }
            mark(4);
            skipBytes(1);
            int i = read();
            if (i == -1) {
                // reached end of stream
                return null;
            }
            if (i == '\r') {
                reset();
                break;
            }
            reset();
            data.write(b);
        }
        skipBytes(3);
        if (encoding != null) {
            return data.toString(encoding);
        } else {
            return data.toString();
        }
    }

    public int copyAll(OutputStream out) throws IOException {
        int count = 0;
        while (true) {
            byte b = readByte();
            if (isBoundary(b)) {
                break;
            }
            out.write(b);
            count++;
        }
        return count;
    }

    public int copyMax(OutputStream out, int maxBytes) throws IOException {
        int count = 0;
        while (true) {
            byte b = readByte();
            if (isBoundary(b)) {
                break;
            }
            out.write(b);
            count++;
            if (count == maxBytes) {
                return count;
            }
        }
        return count;
    }

    public int skipToBoundary() throws IOException {
        int count = 0;
        while (true) {
            byte b = readByte();
            count++;
            if (isBoundary(b)) {
                break;
            }
        }
        return count;
    }

    public boolean isBoundary(byte b) throws IOException {
        int boundaryLen = boundary.length;
        mark(boundaryLen + 1);
        int bpos = 0;
        while (b == boundary[bpos]) {
            b = readByte();
            bpos++;
            if (bpos == boundaryLen) {
                return true;
            }
        }
        reset();
        return false;
    }
}
