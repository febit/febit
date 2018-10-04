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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jodd.net.MimeTypes;
import org.febit.util.agent.LazyAgent;
import org.febit.web.ActionRequest;
import org.febit.web.Filter;
import org.febit.web.render.Text;

/**
 *
 * @author zqq90
 */
public class BearychatFilter implements Filter {

    protected static final Object RESULT_TOKEN_ERR = new Text(MimeTypes.MIME_APPLICATION_JSON, "{\"error\":\"TOKEN_ERR\"}");
    protected String[] tokens;

    protected LazyAgent<Set<String>> _tokensSet = LazyAgent.of(() -> {
        if (tokens == null
                || tokens.length == 0) {
            throw new IllegalStateException("tokens is required");
        }
        return new HashSet<>(Arrays.asList(tokens));
    });

    @Override
    public Object invoke(ActionRequest actionRequest) throws Exception {
        OutgoingMessage message = MessageUtil.getOutgoingMessage(actionRequest);
        if (message == null
                || !checkToken(message)) {
            return RESULT_TOKEN_ERR;
        }
        return actionRequest.invoke();
    }

    protected boolean checkToken(OutgoingMessage message) {
        if (message == null) {
            return false;
        }
        return _tokensSet.get().contains(message.getToken());
    }

}
