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
package org.febit.captcha.web;

import org.febit.util.StringUtil;
import org.febit.web.meta.RenderWith;

/**
 *
 * * @author zqq90
 */
@RenderWith(CaptchaRender.class)
public class CaptchaData {

    public static final String SESSION_KEY_DEFAULT = CaptchaData.class.getName() + ".KEY";

    public static CaptchaData create() {
        return new CaptchaData(SESSION_KEY_DEFAULT);
    }

    public static CaptchaData create(String sessionKey) {
        return new CaptchaData(sessionKey);
    }

    protected String code;
    protected final String sessionKey;

    public CaptchaData(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public boolean verify(String vercode) {
        if (vercode == null) {
            return false;
        }
        return this.code.equals(StringUtil.trimAndUpperDBC(vercode));
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setCode(String code) {
        this.code = StringUtil.trimAndUpperDBC(code);
    }
}
