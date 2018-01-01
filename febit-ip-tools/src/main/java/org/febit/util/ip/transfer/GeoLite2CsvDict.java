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
import java.util.Map;
import java.util.Objects;
import org.febit.lang.IntMap;
import org.febit.lang.Iter;
import org.febit.util.CollectionUtil;
import org.febit.util.CsvUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class GeoLite2CsvDict {

    static final DictEntry UNKNOWN = new DictEntry(-1, "", "", "", "");

    protected static String[] fixLine(String[] line) {

        //0-4 geoname_id,locale_code,continent_code,continent_name,country_iso_code,
        //5-9 country_name,subdivision_1_iso_code,subdivision_1_name,subdivision_2_iso_code,subdivision_2_name,
        //10- city_name,metro_code,time_zone
        return line;
    }

    private final IntMap<DictEntry> map = new IntMap<>();

    public GeoLite2CsvDict() {
    }

    public DictEntry get(int id) {
        return this.map.get(id);
    }

    public Map<String, String> createDictMap(GeoLite2CsvDict dict) {
        Map<String, String> result = CollectionUtil.createMap(this.map.size());
        for (IntMap.Entry<DictEntry> entry : this.map) {
            DictEntry keyEntry = entry.getValue();
            DictEntry valueEntry = dict.get(keyEntry.geoId);
            if (valueEntry == null) {
                continue;
            }
            result.put(keyEntry.countryCode, valueEntry.countryCode);
            result.put(keyEntry.country, valueEntry.country);
            result.put(keyEntry.province, valueEntry.province);
            result.put(keyEntry.city, valueEntry.city);
            result.put(LocationUtil.fixProvince(keyEntry.province), valueEntry.province);
            result.put(LocationUtil.fixCity(keyEntry.city), valueEntry.city);
            result.put(StringUtil.cutSuffix(StringUtil.cutSuffix(keyEntry.city, "市"), "县"), valueEntry.city);
        }
        return result;
    }

    public DictEntry get(String id, String id2) {

        id = id.trim();
        if (id.isEmpty()) {
            //XXX : warning
            id = id2.trim();
        }
        if (id.isEmpty()) {
            return UNKNOWN;
        }
        if (!StringUtil.isNumeric(id)) {
            return null;
        }
        return get(Integer.parseInt(id));
    }

    protected DictEntry createEntry(String[] line) {
        if (line == null || line.length < 11) {
            return null;
        }
        line = fixLine(line);
        //0-4 geoname_id,locale_code,continent_code,continent_name,country_iso_code,
        //5-9 country_name,subdivision_1_iso_code,subdivision_1_name,subdivision_2_iso_code,subdivision_2_name,
        //10- city_name,metro_code,time_zone
        return new DictEntry(Integer.parseInt(line[0]), line[4], line[5], line[7], line[10]);
    }

    protected void add(DictEntry entry) {
        if (entry == null) {
            return;
        }
        this.map.put(entry.geoId, entry);
    }

    public void load(String filepath) throws IOException {
        this.map.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"))) {
            // skip title
            String title = reader.readLine();
            //
            Iter<String[]> lines = CsvUtil.linesIter(reader);
            while (lines.hasNext()) {
                add(createEntry(lines.next()));
            }
        }
    }

    public static class DictEntry {

        final int geoId;
        final String countryCode;
        final String country;
        final String province;
        final String city;

        DictEntry(int geoId, String countryCode, String country, String province, String city) {
            this.geoId = geoId;
            this.province = province;
            this.countryCode = countryCode;
            this.country = country;
            this.city = city;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + this.geoId;
            hash = 47 * hash + Objects.hashCode(this.countryCode);
            hash = 47 * hash + Objects.hashCode(this.country);
            hash = 47 * hash + Objects.hashCode(this.province);
            hash = 47 * hash + Objects.hashCode(this.city);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DictEntry other = (DictEntry) obj;
            if (this.geoId != other.geoId) {
                return false;
            }
            if (!Objects.equals(this.countryCode, other.countryCode)) {
                return false;
            }
            if (!Objects.equals(this.country, other.country)) {
                return false;
            }
            if (!Objects.equals(this.province, other.province)) {
                return false;
            }
            if (!Objects.equals(this.city, other.city)) {
                return false;
            }
            return true;
        }
    }
}
