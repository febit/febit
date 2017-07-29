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

import org.febit.web.ActionRequest;
import org.febit.web.util.ServletUtil;

/**
 *
 * @author zqq90
 */
public class Redirect implements Renderable {

    public static Redirect to(String url) {
        return new Redirect(url, null);
    }

    public static Redirect to(String url, String msg) {
        return new Redirect(url, msg);
    }

    protected final String url;
    protected final String msg;

    public Redirect(String url, String msg) {
        this.url = url;
        this.msg = msg;
    }

    @Override
    public Object render(ActionRequest actionRequest) throws Exception {
        ServletUtil.redirect(actionRequest.request, actionRequest.response, url);
        return null;
    }
}
