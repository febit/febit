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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import jodd.io.StreamUtil;
import org.febit.lang.Iter;
import org.febit.lang.Tuple4;
import org.febit.lang.iter.BaseIter;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class IpSeeker implements Iterable<IpSeeker.Location> {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static IpSeeker create(final String filePath) throws IOException {
        return create(new File(filePath));
    }

    public static IpSeeker create(final File file) throws IOException {
        return create(file.toPath());
    }

    public static IpSeeker create(final Path path) throws IOException {
        return create(Files.readAllBytes(path));
    }

    public static IpSeeker create(final InputStream in) throws IOException {
        return create(StreamUtil.readBytes(in));
    }

    public static IpSeeker create(final byte[] buffer) {

        //Head
        final ByteBuffer headBuf = ByteBuffer.wrap(buffer, 0, 4 * 258);
        final int headPos = headBuf.getInt();
        // we have 256 segments, and last is the end position
        final int[] segmentPos = new int[257];
        for (int i = 0; i < 257; i++) {
            segmentPos[i] = headBuf.getInt();
        }

        //Dict
        final String dictRaw = new String(Arrays.copyOfRange(buffer, headPos, segmentPos[0]), UTF_8);
        final String[] dict = StringUtil.splitc(dictRaw, '\001');

        dict[0] = null; // "None" => null

        //Segments
        final int[][] segments = new int[256][];
        final long[][] segmentIndexers = new long[256][];

        for (int i = 0; i < 256; i++) {
            int start = segmentPos[i];
            int end = segmentPos[i + 1];

            int len = (end - start) / 12; //(1 int + 1 long = 12 byte)

            int[] segment = new int[len];
            long[] segmentIndexer = new long[len];

            ByteBuffer buf = ByteBuffer.wrap(buffer, start, end - start);

            for (int j = 0; j < len; j++) {
                segment[j] = buf.getInt();
                segmentIndexer[j] = buf.getLong();
            }
            segments[i] = segment;
            segmentIndexers[i] = segmentIndexer;
        }

        //XXX 校验
        return new IpSeeker(dict, segments, segmentIndexers);
    }

    public static Iter<Tuple4<Long, Long, Location, Location>> createCompareIter(String file1, String file2) throws IOException {

        final Iter<Location> iter1 = IpSeeker.create(file1).iterator();
        final Iter<Location> iter2 = IpSeeker.create(file2).iterator();
        return createCompareIter(iter1, iter2);
    }

    public static Iter<Tuple4<Long, Long, Location, Location>> createCompareIter(Iter<Location> iter1, Iter<Location> iter2) {
        return new CompareIter(iter1, iter2);
    }

    static class CompareIter extends BaseIter<Tuple4<Long, Long, Location, Location>> {

        final Iter<Location> iter1;
        final Iter<Location> iter2;

        long cursor;
        Location location1;
        Location location2;

        public CompareIter(Iter<Location> iter1, Iter<Location> iter2) {
            this.iter1 = iter1;
            this.iter2 = iter2;
            this.cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return cursor <= IpUtil.IP_MAX;
        }

        @Override
        public Tuple4<Long, Long, Location, Location> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final long form = this.cursor;
            location1 = resureLocation(iter1, location1, form);
            location2 = resureLocation(iter2, location2, form);

            long to = Math.min(location1.getIpTo(), location2.getIpTo());
            this.cursor = to + 1;

            return new Tuple4<>(form, to, location1, location2);
        }

        protected static Location resureLocation(Iter<Location> iter, Location location, long form) {
            while (location == null || location.getIpTo() < form) {
                if (!iter.hasNext()) {
                    return new Location(form, IpUtil.IP_MAX, null, null, null, null);
                }
                location = iter.next();
            }
            return location;
        }
    }

    protected static int findIndex(final int[] datas, final int val) {
        int start = 0;
        int end = datas.length - 1;

        //assert val <= datas[end];
        for (;;) {
            if (val <= datas[start]) {
                return start;
            }
            if (val > datas[end]) {
                return end + 1;
            }

            if (end - start <= 1) {
                return end;
            }

            int mid = (start + end) / 2;
            long midIp = datas[mid];

            if (val < midIp) {
                end = mid;
            } else if (val > midIp) {
                start = mid;
            } else {
                return mid;
            }
        }
    }

    protected static int resolveSegment(long ip) {
        return (int) ((ip >>> 24) & 0xFF);
    }

    protected static int resolveLitteIP(long ip) {
        return (int) (ip & 0xFFFFFF);
    }

    protected final String[] originDict;
    protected final String[] dict;
    protected final int[][] segments;
    protected final long[][] segmentIndexers;

    protected IpSeeker(String[] dict, int[][] segments, long[][] segmentIndexer) {
        this(null, dict, segments, segmentIndexer);
    }

    protected IpSeeker(String[] originDict, String[] dict, int[][] segments, long[][] segmentIndexer) {
        this.originDict = originDict != null ? originDict : dict;
        this.dict = dict;
        this.segments = segments;
        this.segmentIndexers = segmentIndexer;
    }

    public String[] getDict() {
        String[] copy = new String[this.dict.length];
        System.arraycopy(this.dict, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Replace dict with given map.
     *
     * @param map
     * @return count of replaced
     */
    public IpSeeker i18n(Map<String, String> map) {

        //int replacedCount = 0;
        final String[] origin = this.originDict;
        final String[] destDict = new String[origin.length];

        for (int i = 0; i < origin.length; i++) {
            String word = map.get(origin[i]);
            if (word != null) {
                destDict[i] = word;
                //replacedCount++;
            } else {
                destDict[i] = origin[i];
            }
        }
        return new IpSeeker(originDict, destDict, segments, segmentIndexers);
    }

    public Location locate(String ipv4) {
        return locate(IpUtil.parseInt(ipv4));
    }

    public Location locate(int ipv4) {
        final int segment = (ipv4 >>> 24) & 0xFF;
        final int little = ipv4 & 0xFFFFFF;
        return locate(segment, little);
    }

    public Location locate(long ipv4) {
        int segment = (int) ((ipv4 >>> 24) & 0xFF);
        int little = (int) (ipv4 & 0x00FFFFFFL);
        return locate(segment, little);
    }

    protected Location locate(final int segment, final int little) {
        int index = findIndex(segments[segment], little);
        return createLocationByIndex(segment, index);
    }

    protected Location createLocationByIndex(final int segment, final int index) {
        long key = segmentIndexers[segment][index];

        long segmentHead = ((long) (segment)) << 24;
        long ipTo = segmentHead | segments[segment][index];
        long ipFrom;
        if (index != 0) {
            ipFrom = (segmentHead | segments[segment][index - 1]) + 1;
        } else {
            ipFrom = segmentHead;
        }
        if (key == 0L) {
            return new Location(ipFrom, ipTo, null, null, null, null);
        }
        return new Location(
                ipFrom,
                ipTo,
                dict[(int) ((key >>> 48) & 0xFFFF)],
                dict[(int) ((key >>> 32) & 0xFFFF)],
                dict[(int) ((key >>> 16) & 0xFFFF)],
                dict[(int) ((key) & 0xFFFF)]
        );
    }

    @Override
    public Iter<Location> iterator() {

        return new BaseIter<Location>() {

            int segment = 0;
            int index = 0;

            protected void fixCursor() {
                if (index == 0) {
                    //Note: 每个segment都至少有一个值,
                    //      因此, 为0 时不需要fix, 
                    //      注意: 并不表示
                    return;
                }

                //确保超出后 
                //    segment = 256
                //    index = 0
                if (segment > 255) {
                    index = 0;
                    return;
                }

                if (index >= IpSeeker.this.segments[segment].length) {
                    segment++;
                    index = 0;
                }
            }

            @Override
            public boolean hasNext() {
                //Note: 快速判断: 每个segment都至少有一个值,
                //      因此, 只要不是最后一个segment都有值
                if (segment < 255) {
                    return true;
                }

                //修正进度
                fixCursor();
                //fix 后 只要segment在正常范围,就会有值
                if (segment <= 255) {
                    return true;
                }
                return false;
            }

            @Override
            public Location next() {
                fixCursor();
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Location location = IpSeeker.this.createLocationByIndex(segment, index);
                index++;
                return location;
            }
        };

    }

    public static final class Location {

        private final long ipFrom;
        private final long ipTo;
        private final String country;
        private final String isp;
        private final String province;
        private final String city;

        public Location(long ipFrom, long ipTo, String country, String isp, String province, String city) {
            this.ipFrom = ipFrom;
            this.ipTo = ipTo;
            this.country = country;
            this.isp = isp;
            this.province = province;
            this.city = city;
        }

        public long getIpFrom() {
            return ipFrom;
        }

        public long getIpTo() {
            return ipTo;
        }

        public String getIpFromString() {
            return IpUtil.toString(this.ipFrom);
        }

        public String getIpToString() {
            return IpUtil.toString(this.ipTo);
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
         * 运营商
         */
        public String getIsp() {
            return isp;
        }

        @Override
        public String toString() {
            return this.country + ' ' + this.province + ' ' + this.city + ' ' + this.isp;
        }
    }

}
