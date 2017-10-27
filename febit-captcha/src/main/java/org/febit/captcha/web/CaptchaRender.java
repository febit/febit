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

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jodd.io.StreamUtil;
import org.febit.captcha.Captcha;
import org.febit.web.ActionRequest;
import org.febit.web.render.Render;
import org.febit.web.util.ServletUtil;

/**
 *
 * * @author zqq90
 */
public class CaptchaRender implements Render<CaptchaData> {

    private Captcha captcha;

    @Override
    public Object render(ActionRequest actionRequest, CaptchaData resultValue) throws Exception {
        final HttpServletResponse response = actionRequest.response;
        final HttpSession session = actionRequest.request.getSession();

        final String capText = captcha.createText();
        resultValue.setCode(capText);
        session.setAttribute(resultValue.getSessionKey(), resultValue);

        ServletUtil.preventCaching(response);
        response.setContentType("image/jpg");
        OutputStream out = null;
        try {
            captcha.write(capText, out = response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Exception when create captcha image ", e);
        } finally {
            StreamUtil.close(out);
        }
        return null;
    }
}
