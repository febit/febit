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

import javax.servlet.http.HttpSession;
import org.febit.service.ServiceResult;
import org.febit.web.ActionRequest;
import org.febit.web.Filter;

/**
 *
 * * @author zqq90
 */
public class CaptchaFilter implements Filter {

    protected String sessionKey = CaptchaData.SESSION_KEY_DEFAULT;
    protected String paramKey = "__vercode";
    protected boolean removeAfterVerify = true;

    @Override
    public Object invoke(ActionRequest actionRequest) throws Exception {
        HttpSession session = actionRequest.request.getSession();
        String vercode = actionRequest.getParameter(paramKey);
        if (vercode == null || vercode.isEmpty()) {
            return onInputEmpty(actionRequest);
        }
        CaptchaData real = (CaptchaData) session.getAttribute(sessionKey);
        if (removeAfterVerify) {
            session.setAttribute(sessionKey, null);
        }
        if (real != null && real.verify(vercode)) {
            return actionRequest.invoke();
        }
        return onCodeInvalid(actionRequest);
    }

    protected Object onCodeInvalid(ActionRequest actionRequest) {
        return ServiceResult.error(ServiceResult.ERROR_VERCODE);
    }

    protected Object onInputEmpty(ActionRequest actionRequest) {
        return ServiceResult.error(ServiceResult.ERROR_VERCODE);
    }
}
