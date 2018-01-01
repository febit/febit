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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.febit.lang.Tuple3;

/**
 *
 * @author zqq90
 */
public class LocationUtil {

    static final Map<String, String> COUNTRY_CODE_ALIAS_MAP = new HashMap<>();
    static final Map<String, String> PROVINCE_ALIAS_MAP = new HashMap<>();
    static final Map<String, String> CITY_ALIAS_MAP = new HashMap<>();

    static final Set<String> SPEC_AREA_SET = new HashSet<>();

    static {

        //Country
        COUNTRY_CODE_ALIAS_MAP.put("中国", "CN");
        COUNTRY_CODE_ALIAS_MAP.put("中国澳门", "MO");
        COUNTRY_CODE_ALIAS_MAP.put("中国台湾", "TW");
        COUNTRY_CODE_ALIAS_MAP.put("中国香港", "HK");
        COUNTRY_CODE_ALIAS_MAP.put("澳门", "MO");
        COUNTRY_CODE_ALIAS_MAP.put("台湾", "TW");
        COUNTRY_CODE_ALIAS_MAP.put("香港", "HK");
        COUNTRY_CODE_ALIAS_MAP.put("IPIP.NET", "");

        //Province
        PROVINCE_ALIAS_MAP.put("广西壮族自治区", "广西");
        PROVINCE_ALIAS_MAP.put("西藏自治区", "西藏");
        PROVINCE_ALIAS_MAP.put("内蒙古自治区", "内蒙古");
        PROVINCE_ALIAS_MAP.put("宁夏回族自治区", "宁夏");
        PROVINCE_ALIAS_MAP.put("新疆维吾尔自治区", "新疆");

        PROVINCE_ALIAS_MAP.put("中国澳门", "澳门");
        PROVINCE_ALIAS_MAP.put("中国台湾", "台湾");
        PROVINCE_ALIAS_MAP.put("中国香港", "香港");
        PROVINCE_ALIAS_MAP.put("中国", "");

        PROVINCE_ALIAS_MAP.put("北京市", "北京");
        PROVINCE_ALIAS_MAP.put("上海市", "上海");
        PROVINCE_ALIAS_MAP.put("天津市", "天津");
        PROVINCE_ALIAS_MAP.put("重庆市", "重庆");

        CITY_ALIAS_MAP.put("中国", null);

        SPEC_AREA_SET.add("北京");
        SPEC_AREA_SET.add("上海");
        SPEC_AREA_SET.add("天津");
        SPEC_AREA_SET.add("重庆");
        SPEC_AREA_SET.add("香港");
        SPEC_AREA_SET.add("澳门");

    }

    public static String fixCountryCode(String country) {
        if (country == null) {
            return null;
        }
        String fixed = COUNTRY_CODE_ALIAS_MAP.get(country);
        if (fixed != null) {
            if (fixed.isEmpty()) {
                return null;
            }
            country = fixed;
        }
        return country;
    }

    public static String fixProvince(String province) {
        if (province == null) {
            return null;
        }
        if (province.endsWith("省")) {
            province = province.substring(0, province.length() - 1);
        }
        String fixed = PROVINCE_ALIAS_MAP.get(province);
        if (fixed != null) {
            if (fixed.isEmpty()) {
                return null;
            }
            province = fixed;
        }
        return province;
    }

    public static String fixCity(String city) {
        if (city == null) {
            return null;
        }
        String fixed = CITY_ALIAS_MAP.get(city);
        if (fixed != null) {
            if (fixed.isEmpty()) {
                return null;
            }
            city = fixed;
        }
        return city;
    }

    public static String fixArea(String area) {
        return area;
    }

    public static Tuple3<String, String, String> fixLocation(String country, String province, String city) {

        country = fixCountryCode(country);
        province = fixProvince(province);
        city = fixCity(city);

        //Note: Maybe spec province 
        String spec = COUNTRY_CODE_ALIAS_MAP.get(province);
        if (spec != null) {
            country = spec;
        }

        if (country == null) {
            return new Tuple3<>(null, null, null);
        }
        if (SPEC_AREA_SET.contains(province)) {
            return new Tuple3<>(country, province, province);
        }
        if (SPEC_AREA_SET.contains(city)) {
            return new Tuple3<>(country, city, city);
        }
        return new Tuple3<>(country, province, city);
    }
}
