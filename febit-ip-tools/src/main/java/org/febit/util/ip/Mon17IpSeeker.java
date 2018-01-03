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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.febit.lang.iter.BaseIter;

/**
 * 数据库相关链接: http://www.ipip.net/download.html .
 *
 */
public final class Mon17IpSeeker {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Location UNKNOWN_LOCATION = new Location("未知\t未知\t未知\t未知");

    private final byte[] buffer;
    private final int[] fastIndex;
    private final int offsetLimit;

    private Mon17IpSeeker(final byte[] buffer) {
        this.buffer = buffer;
        this.offsetLimit = getIntB(0);

        final int[] index = new int[256];
        for (int i = 0; i < 256; i++) {
            // fastindex(+1024) head(+4)
            index[i] = (getIntL(4 + (i << 2)) << 3) + 1024 + 4;
        }
        this.fastIndex = index;
    }

    public static Mon17IpSeeker create(final String filePath) throws IOException {
        return create(new File(filePath));
    }

    public static Mon17IpSeeker create(final File file) throws IOException {
        return create(file.toPath());
    }

    public static Mon17IpSeeker create(final Path path) throws IOException {
        return create(Files.readAllBytes(path));
    }

    public static Mon17IpSeeker create(final byte[] buffer) {
        return new Mon17IpSeeker(buffer);
    }

    /**
     * Locate given ip.
     *
     * @param ipv4
     * @return
     * @throws UnknownHostException
     */
    public Location locate(final String ipv4) throws UnknownHostException {
        return locate(Inet4Address.getByName(ipv4));
    }

    /**
     * Locate given ip.
     *
     * @param ip
     * @return
     */
    public Location locate(final InetAddress ip) {
        return (ip instanceof Inet4Address) ? locate(ip.getAddress()) : UNKNOWN_LOCATION;
    }

    /**
     * Locate given ip.
     *
     * @param ipv4
     * @return
     */
    public Location locate(final byte[] ipv4) {
        if (ipv4 == null || ipv4.length != 4) {
            return UNKNOWN_LOCATION;
        }
        return locate(makeInt(ipv4[0], ipv4[1], ipv4[2], ipv4[3]));
    }

    /**
     *
     * Locate given ip.
     *
     * @param b0
     * @param b1
     * @param b2
     * @param b3
     * @return
     */
    public Location locate(final byte b0, final byte b1, final byte b2, final byte b3) {
        return locate(makeInt(b0, b1, b2, b3));
    }

    /**
     * Locate given ip.
     *
     * @param ipv4
     * @return
     */
    public Location locate(final int ipv4) {
        final long ip = IpUtil.int2long(ipv4);
        return locate(ip);
    }

    public Location locate(final long ipv4) {
        final int maxCompIndex = this.offsetLimit;
        int offset = getSegmentOffset((int) ((ipv4 >> 24) & 0xFF));
        for (; offset < maxCompIndex; offset += 8) {
            if (IpUtil.int2long(getIntB(offset)) < ipv4) {
                continue;
            }
            return createLocationAtOffset(offset);
        }
        return UNKNOWN_LOCATION;
    }

    protected int getSegmentOffset(int segment) {
        return this.fastIndex[segment];
    }

    private int getIntL(final int offset) {
        final byte[] buf = this.buffer;
        return makeInt(buf[offset + 3],
                buf[offset + 2],
                buf[offset + 1],
                buf[offset]);
    }

    private int getIntB(final int offset) {
        final byte[] buf = this.buffer;
        return makeInt(buf[offset],
                buf[offset + 1],
                buf[offset + 2],
                buf[offset + 3]);
    }

    protected Location createLocationAtOffset(int offset) {
        final byte[] buf = this.buffer;
        return new Location(new String(buf,
                this.offsetLimit - 1024 + makeInt((byte) 0, buf[offset + 6], buf[offset + 5], buf[offset + 4]),
                buf[offset + 7] & 0xFF,
                UTF_8));
    }
    
    private static int makeInt(final byte b0, final byte b1, final byte b2, final byte b3) {
        return ((b0 & 0xFF) << 24)
                | ((b1 & 0xFF) << 16)
                | ((b2 & 0xFF) << 8)
                | ((b3 & 0xFF));
    }

    public BaseIter<LocationIP> createIterator() {
        return new Iter(getSegmentOffset(0));
    }

    protected class Iter extends BaseIter<LocationIP> {

        protected int cursor;
        protected long last = -1;

        public Iter(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return this.cursor < Mon17IpSeeker.this.offsetLimit
                    && getIp() >= this.last;
        }

        protected long getIp() {
            return IpUtil.int2long(getIntB(this.cursor));
        }

        @Override
        public LocationIP next() {
            long next = getIp();
            LocationIP locationIP = new LocationIP(last + 1, next, createLocationAtOffset(this.cursor));
            this.last = next;
            this.cursor += 8;
            return locationIP;
        }
    }

    public static final class LocationIP {

        protected final long start;
        protected final long end;
        protected final Location location;

        public LocationIP(long start, long end, Location location) {
            this.start = start;
            this.end = end;
            this.location = location;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public String getCountry() {
            return this.location.country;
        }

        public String getProvince() {
            return this.location.province;
        }

        public String getCity() {
            return this.location.city;
        }

        public String getUnit() {
            return this.location.unit;
        }
    }

    public static final class Location {

        private final String country;
        private final String province;
        private final String city;
        private final String unit;

        private Location(final String location) {
            final String[] split = location.split("\t", 4);
            this.country = split[0];
            this.province = split[1];
            this.city = split[2];
            this.unit = split[3];
        }

        /**
         * 国家
         */
        public String getCountry() {
            return country;
        }

        /**
         * 省
         */
        public String getProvince() {
            return province;
        }

        /**
         * 市
         */
        public String getCity() {
            return city;
        }

        /**
         * 学校/单位
         */
        public String getUnit() {
            return unit;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s %s", this.country, this.province, this.city, this.unit).trim();
        }
    }
}
