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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.febit.lang.Iter;
import org.febit.util.CsvUtil;
import org.febit.util.ip.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GeoLite2-City-CSV.
 *
 * Note: 跳过首行标题
 * network,geoname_id,registered_country_geoname_id,represented_country_geoname_id,is_anonymous_proxy,is_satellite_provider,postal_code,latitude,longitude,accuracy_radius
 *
 * @author zqq90
 */
public class GeoLite2Csv {

    private static final Logger LOG = LoggerFactory.getLogger(GeoLite2Csv.class);

    public static Iter<TransferInput> createIteratorFromFile(String filepath, String dictFile) throws IOException {
        GeoLite2CsvDict dict = new GeoLite2CsvDict();
        dict.load(dictFile);
        return createIteratorFromFile(filepath, dict);
    }

    public static Iter<TransferInput> createIteratorFromFile(String filepath, final GeoLite2CsvDict dict) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"))) {
            //skip title
            reader.readLine();
            return CsvUtil.linesIter(reader)
                    .map(line -> createTransferInput(line, dict))
                    .excludeNull();
        }
    }

    public static TransferInput createTransferInput(String[] arr, GeoLite2CsvDict dict) {
        if (arr == null) {
            return null;
        }

        //IP
        String ipSegment = arr[0];

        int split = ipSegment.indexOf('/');
        long ipSegmentHead = IpUtil.parseLong(ipSegment.substring(0, split));
        int ipSegmentMark = Integer.parseInt(ipSegment.substring(split + 1));

        long start = getSegmentStart(ipSegmentHead, ipSegmentMark);
        long end = getSegmentEnd(ipSegmentHead, ipSegmentMark);

        GeoLite2CsvDict.DictEntry location = end != IpUtil.IP_MAX ? dict.get(arr[1], arr[2]) : GeoLite2CsvDict.UNKNOWN;
        // location
        if (location == null) {
            LOG.debug("Dict not found, line: {}", CsvUtil.toCsvString(arr));
            return null;
        }

        return new TransferInputImpl(start, end, location.countryCode, null, location.province, location.city);
    }

    private static long getSegmentStart(final long ip, final int mark) {
        int i = 32 - mark;
        if (i <= 0) {
            return ip;
        }
        return ip & (IpUtil.IP_MAX << i);
    } 

    private static long getSegmentEnd(final long ip, final int mark) {
        int i = 32 - mark;
        if (i <= 0) {
            return ip;
        }
        return ip | ((1L << i) - 1);
    }
}
