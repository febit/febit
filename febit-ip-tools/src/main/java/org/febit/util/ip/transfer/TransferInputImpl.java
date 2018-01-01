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

import org.febit.lang.Tuple3;

/**
 *
 * @author zqq90
 */
public class TransferInputImpl implements TransferInput {

    protected long from;
    protected long to;
    protected String country;
    protected String isp;
    protected String province;
    protected String city;

    public TransferInputImpl() {
    }

    public TransferInputImpl(long from, long to, String country, String isp, String province, String city) {
        this.from = from;
        this.to = to;
        this.isp = isp;

        Tuple3<String, String, String> fixed = LocationUtil.fixLocation(country, province, city);
        this.country = fixed._1;
        this.province = fixed._2;
        this.city = fixed._3;
    }

    @Override
    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    @Override
    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    @Override
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    @Override
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Override
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
