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
package org.febit.util.ip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import jodd.io.StreamUtil;

/**
 *
 * @author zqq90
 */
public class IpBlankSeeker {

    protected final long[] _datas;

    protected IpBlankSeeker(long datas[]) {
        this._datas = datas;
    }

    public boolean isWhite(String ipv4) {
        return isWhite(IpUtil.parseLong(ipv4));
    }

    public boolean isWhite(int ipv4) {
        return isWhite(IpUtil.int2long(ipv4));
    }

    public boolean isWhite(long ipv4) {
        int index = findIndex(ipv4);
        //System.out.println(IpUtil.ipToString(ipv4) +  " index="+index +" " 
        //        +(index >0 ? IpUtil.ipToString(this._datas[index-1]): "0.0.0.0") 
        //        +(index <this._datas.length ? IpUtil.ipToString(this._datas[index]): "255.255.255.255")
        //);
        return index >= 0 && (index & 1) == 1;
    }

    protected int findIndex(long ipv4) {
        final long[] datas = this._datas;
        int start = 0;
        int end = datas.length - 1;

        if (ipv4 < datas[start]
                || ipv4 > datas[end]) {
            return -1;
        }
        for (;;) {
            if (ipv4 <= datas[start]) {
                return start;
            }
            if (ipv4 > datas[end]) {
                return end + 1;
            }

            if (start + 1 >= end) {
                return end;
            }

            int mid = (start + end) / 2;
            long midIp = datas[mid];

            if (ipv4 < midIp) {
                end = mid;
            } else if (ipv4 > midIp) {
                start = mid;
            } else {
                return mid;
            }
        }
    }

    public static IpBlankSeeker create(final String filePath) throws IOException {
        return create(new File(filePath));
    }

    public static IpBlankSeeker create(final File file) throws IOException {
        return create(file.toPath());
    }

    public static IpBlankSeeker create(final Path path) throws IOException {
        return create(Files.readAllBytes(path));
    }

    public static IpBlankSeeker create(InputStream in) throws IOException {
        return create(StreamUtil.readBytes(in));
    }

    public static IpBlankSeeker create(final byte[] buffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        int len = byteBuffer.getInt();
        long[] result = new long[len];
        long last = 0;
        for (int i = 0; i < len; i++) {
            result[i] = IpUtil.int2long(byteBuffer.getInt());
            assert last < result[i];
            last = result[i];
        }
        return new IpBlankSeeker(result);
    }
}
