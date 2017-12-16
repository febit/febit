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
package org.febit.web.component;

import org.febit.lang.ConcurrentIdentityMap;
import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.web.ActionRequest;
import org.febit.web.meta.RenderWith;
import org.febit.web.render.Render;
import org.febit.web.util.MatchTypes;

/**
 *
 * @author zqq90
 */
@SuppressWarnings("unchecked")
public class RenderManager implements Component {

    protected final ConcurrentIdentityMap<Class, Render> renderCache;

    protected Render[] renders;
    protected Render defaultRender;

    protected Petite petite;

    public RenderManager() {
        this.renderCache = new ConcurrentIdentityMap<>();
    }

    @Petite.Init
    public void init() {
        if (renders != null) {
            for (Render render : renders) {
                if (render instanceof MatchTypes) {
                    for (Class type : ((MatchTypes) render).matchTypes()) {
                        renderCache.put(type, render);
                    }
                }
                renderCache.put(render.getClass(), render);
            }
        }
    }

    protected Render resolveRender(final ActionRequest actionRequest, final Object result) {
        Render render = null;
        for (Class<?> type : ClassUtil.impls(result.getClass())) {
            render = renderCache.get(type);
            if (render != null) {
                break;
            }
            RenderWith anno = type.getAnnotation(RenderWith.class);
            if (anno != null) {
                Class renderClass = anno.value();
                render = renderCache.get(renderClass);
                if (render != null) {
                    break;
                }
                render = (Render) petite.get(renderClass);
                if (render != null) {
                    render = renderCache.putIfAbsent(renderClass, render);
                    break;
                }
            }
        }
        if (render == null) {
            render = defaultRender;
        }
        return renderCache.putIfAbsent(result.getClass(), render);
    }

    public void render(final ActionRequest actionRequest, Object result) throws Exception {
        if (result != null) {
            int count = 0;
            do {
                count++;
                Render render = renderCache.get(result.getClass());
                if (render == null) {
                    render = resolveRender(actionRequest, result);
                }
                result = render.render(actionRequest, result);
            } while (result != null && count < 100);
        } else {
            defaultRender.render(actionRequest, result);
        }
    }
}
