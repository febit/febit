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
package org.febit.bearychat;

import jodd.json.meta.JSON;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class OutgoingMessage {

    @JSON(name = "token")
    protected String token;
    @JSON(name = "ts")
    protected long ts;
    @JSON(name = "text")
    protected String text;
    @JSON(name = "trigger_word")
    protected String triggerWord;
    @JSON(name = "subdomain")
    protected String subdomain;
    @JSON(name = "channel_name")
    protected String channelName;
    @JSON(name = "user_name")
    protected String userName;

    @JSON(include = false)
    public String getFixedText() {
        String res = text;
        if (res == null) {
            return "";
        }
        if (triggerWord != null) {
            res = StringUtil.cutPrefix(res, triggerWord);
        }
        return res.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTriggerWord() {
        return triggerWord;
    }

    public void setTriggerWord(String triggerWord) {
        this.triggerWord = triggerWord;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
