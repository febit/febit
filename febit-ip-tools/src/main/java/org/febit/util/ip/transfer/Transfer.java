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
package org.febit.util.ip.transfer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import jodd.io.StreamUtil;
import org.febit.util.StringUtil;
import org.febit.util.ip.IpUtil;

/**
 *
 * @author zqq90
 */
public class Transfer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Transfer.class);

    protected static final int IP_PART_MAX = 0x00FFFFFF;

    protected static int resolveSegment(long ip) {
        return (int) ((ip >>> 24) & 0xFF);
    }

    protected static int resolveLitteIP(long ip) {
        return (int) (ip & 0xFFFFFF);
    }

    protected static int writeInt(BufferedOutputStream out, int number) throws IOException {
        out.write((number >>> 24) & 0xFF);
        out.write((number >>> 16) & 0xFF);
        out.write((number >>> 8) & 0xFF);
        out.write(number & 0xFF);
        return 4;
    }

    protected static int writeShort(BufferedOutputStream out, int number) throws IOException {
        out.write((number >>> 8) & 0xFF);
        out.write(number & 0xFF);
        return 2;
    }

    protected final LinkedHashMap<String, Integer> dict;
    protected final ArrayList<Entry> entrys;
    protected long last;

    public Transfer() {
        this.entrys = new ArrayList<>(500000);
        this.dict = new LinkedHashMap<>(1000);
        this.dict.put("None", 0);
        this.last = -1L;
    }

    public int getTotalSize() {
        return entrys.size();
    }

    public int getDictSize() {
        return dict.size();
    }

    public void start(final String filepath, final String outpath) throws IOException {

        LOG.info("Amount of entrys: {}", entrys.size());
        LOG.info("Amount of dicts : {}", dict.size());
        emit(outpath);
        LOG.info("done!");
    }

    protected int resoveDictIndex(String string) {
        if (string == null) {
            return 0;
        }
        string = string.trim();
        if (string.isEmpty()) {
            return 0;
        }
        string = StringUtil.remove(string, '\001');
        Integer index = dict.get(string);
        if (index == null) {
            index = dict.size();
            dict.put(string, index);
        }
        return index;
    }

    public void read(final Iterator<TransferInput> iter) throws IOException {

        while (iter.hasNext()) {
            addEntry(iter.next());
        }

        //fix last
        if (this.last != IpUtil.IP_MAX) {
            addEntry(this.last + 1, IpUtil.IP_MAX, 0, 0, 0, 0);
        }

        //check size of indexer
        if (dict.size() >= 65025) {
            throw new RuntimeException("Failed: indexer overflow !!");
        }
    }

    protected void addEntry(TransferInput info) {
        addEntry(info.getFrom(), info.getTo(),
                resoveDictIndex(info.getCountry()),
                resoveDictIndex(info.getIsp()),
                resoveDictIndex(info.getProvince()),
                resoveDictIndex(info.getCity())
        );
    }

    protected void addEntry(long from, long to, int country, int isp, int province, int city) {
        if (from > to) {
            throw new RuntimeException(StringUtil.format("from > to : {} > {} ", from, to));
        }
        if (this.last + 1 != from) {
            if (this.last + 1 < from) {
                //fix lost part
                LOG.debug("Fix lost part: {} - {}" + from, (this.last + 1), from);
                addEntry(this.last + 1, from - 1, 0, 0, 0, 0);
            } else {
                throw new RuntimeException(StringUtil.format("Not increated, except {}, but got {}", this.last + 1, from));
            }
        }

        final int toSegment = resolveSegment(to);
        final int toLIP = resolveLitteIP(to);

        int startSegment = resolveSegment(from);
        while (startSegment < toSegment) {
            //填满跨越的段
            LOG.debug("Fill segment: {}", startSegment);
            push(new Entry(startSegment, IP_PART_MAX, country, isp, province, city));
            startSegment++;
        }
        push(new Entry(toSegment, toLIP, country, isp, province, city));

        //END
        this.last = to;
    }

    protected void push(Entry entry) {

        //判断是否可以合并
        if (!entrys.isEmpty()) {
            Entry lastEntry = entrys.get(entrys.size() - 1);
            if (lastEntry.canCombine(entry)) {
                entrys.set(entrys.size() - 1, entry);
                return;
            }
        }

        //否则直接添加
        entrys.add(entry);
    }

    public void emit(final String outpath) throws IOException {

        final Charset charset = Charset.forName("UTF-8");
        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outpath));

        int bytesSize = 0;

        int[] segmentPos = new int[256];

        //Header
        bytesSize += writeInt(out, 0);

        //ip segment header
        for (int i = 0; i < 257; i++) {
            bytesSize += writeInt(out, 0);
        }

        int dictStart = bytesSize;
        boolean notfirst = false;
        for (String area : dict.keySet()) {
            if (notfirst) {
                out.write('\001');
                bytesSize++;
            } else {
                notfirst = true;
            }
            byte[] bytes = area.getBytes(charset);
            out.write(bytes);
            bytesSize += bytes.length;
        }

        LOG.info("head end size: {}", bytesSize);

        segmentPos[0] = bytesSize;

        int currSegment = 0;

        for (Entry entry : entrys) {
            if (entry.segment != currSegment) {
                currSegment = entry.segment;
                segmentPos[currSegment] = bytesSize;
            }

            bytesSize += writeInt(out, entry.lip);

            bytesSize += writeShort(out, entry.country);
            bytesSize += writeShort(out, entry.isp);

            bytesSize += writeShort(out, entry.province);
            bytesSize += writeShort(out, entry.city);
        }

        StreamUtil.close(out);

        try (RandomAccessFile file = new RandomAccessFile(outpath, "rw")) {
            //Head start
            file.writeInt(dictStart);
            //segmentPos
            for (int pos : segmentPos) {
                file.writeInt(pos);
            }
            //last segment end pos
            file.writeInt(bytesSize);
        }
    }

    protected static class Entry {

        public final int segment;
        public final int lip;
        public final int country;
        public final int isp;
        public final int province;
        public final int city;

        public Entry(int segment, int lip, int country, int isp, int province, int city) {
            this.segment = segment;
            this.lip = lip;
            this.country = country;
            this.isp = isp;
            this.province = province;
            this.city = city;
        }

        public boolean canCombine(Entry other) {

            if (this.segment != other.segment) {
                return false;
            }
            if (!isSameLocation(other)) {
                return false;
            }
            return true;
        }

        public boolean isSameLocation(Entry other) {
            if (this.isp != other.isp) {
                return false;
            }
            if (this.country != other.country) {
                return false;
            }
            if (this.province != other.province) {
                return false;
            }
            if (this.city != other.city) {
                return false;
            }
            return true;
        }
    }

}
