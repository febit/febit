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

import javax.servlet.http.HttpServletRequest;
import jodd.io.StreamUtil;
import jodd.json.JsonParser;
import jodd.util.StringPool;
import org.febit.web.ActionRequest;

/**
 *
 * @author zqq90
 */
public class MessageUtil {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MessageUtil.class);
    protected static final String KEY_OUT_MSG = "$$KEY_OUT_MSG";

    public static OutgoingMessage getOutgoingMessage(ActionRequest request) {
        Object obj = request.request.getAttribute(KEY_OUT_MSG);
        if (obj instanceof OutgoingMessage) {
            return (OutgoingMessage) obj;
        }
        OutgoingMessage msg = parseOutgoingMessage(request);
        if (obj == null) {
            request.request.setAttribute(KEY_OUT_MSG, msg);
        }
        return msg;
    }

    public static OutgoingMessage parseOutgoingMessage(ActionRequest request) {
        char[] raw = readInput(request.request);
        if (raw == null) {
            return null;
        }
        OutgoingMessage msg;
        try {
            msg = JsonParser.create().parse(raw, OutgoingMessage.class);
        } catch (Exception e) {
            LOG.error("PARSE_OUT_MSG_ERROR:", e);
            msg = null;
        }
        return msg;
    }

    protected static char[] readInput(HttpServletRequest request) {
        try {
            return StreamUtil.readChars(request.getInputStream(), StringPool.UTF_8);
        } catch (Exception e) {
            LOG.error("READ_DATA_ERROR:", e);
            return null;
        }
    }
}
