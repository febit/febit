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

import org.febit.web.meta.RenderWith;

/**
 *
 * @author zqq90
 */
@RenderWith(InternalForwardRender.class)
public class InternalForward {

    public static InternalForward relative(String path) {
        return new InternalForward(path, true);
    }

    public static InternalForward absolute(String path) {
        return new InternalForward(path, false);
    }

    protected final String path;
    protected final boolean relative;

    protected InternalForward(String path, boolean relative) {
        this.path = path;
        this.relative = relative;
    }

    public String getPath() {
        return path;
    }

    public boolean isRelative() {
        return relative;
    }

}
