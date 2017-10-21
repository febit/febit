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
package org.febit.web.render;

import jodd.io.FileNameUtil;
import org.febit.web.ActionRequest;
import org.febit.web.component.ActionManager;

/**
 *
 * @author zqq90
 */
public class InternalForwardRender implements Render<InternalForward> {

    protected ActionManager actionManager;

    @Override
    public Object render(ActionRequest actionRequest, InternalForward result) throws Exception {
        String realPath = result.isRelative()
                ? FileNameUtil.concat(actionRequest.actionConfig.path, result.getPath(), true)
                : result.getPath();
        ActionRequest forwardRequest = actionManager.buildActionRequest(actionRequest.request.getMethod(), realPath, actionRequest.request, actionRequest.response);
        if (forwardRequest == null) {
            return ResponeError.ERROR_404;
        }
        forwardRequest.invoke();
        // returns Noop to avoid render twice
        return Noop.INSTANCE;
    }
}
