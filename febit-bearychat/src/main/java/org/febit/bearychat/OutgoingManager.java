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

import org.febit.bearychat.meta.Outgoing;
import org.febit.shaded.jodd.macro.BasePathMacros;
import org.febit.shaded.jodd.macro.RegExpPathMacros;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jodd.paramo.MethodParameter;
import jodd.paramo.Paramo;
import org.febit.convert.Convert;
import org.febit.util.ArraysUtil;
import org.febit.util.ClassUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class OutgoingManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OutgoingManager.class);
    protected final static String[] MACRO_SEPARATORS = {"{", ":", "}"};

    protected final Map<String, HandlerConfig> directHandlers = new HashMap<>();
    protected final List<HandlerConfig> handlers = new ArrayList<>();

    public void register(Class type) {
        OutgoingManager.this.register(null, type.getMethods());
    }

    public void register(Object action) {
        if (action instanceof Class) {
            OutgoingManager.this.register((Class) action);
        } else {
            OutgoingManager.this.register(action, action.getClass().getMethods());
        }
    }

    protected void register(Object action, Method[] methods) {
        for (Method method : methods) {
            Outgoing actionAnno = method.getAnnotation(Outgoing.class);
            if (actionAnno == null) {
                continue;
            }
            for (String macro : actionAnno.value()) {
                OutgoingManager.this.register(action, method, macro);
            }
        }
        Collections.sort(handlers);
    }

    protected void register(Object action, Method method, String macro) {
        HandlerConfig config = HandlerConfig.create(action, method, macro);
        if (!config.hasMacros()) {
            directHandlers.put(macro, config);
        }
        handlers.add(config);
    }

    public Object invoke(OutgoingMessage msg) throws Exception {
        String text = msg.getFixedText();
        HandlerConfig config = directHandlers.get(text);
        for (Iterator<HandlerConfig> iterator = handlers.iterator(); config == null && iterator.hasNext();) {
            HandlerConfig next = iterator.next();
            if (next.isMatch(text)) {
                config = next;
                break;
            }
        }
        if (config == null) {
            LOG.info("Not found handler for: {}", text);
            return null;
        }
        try {
            return config.invoke(msg);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            while (cause instanceof InvocationTargetException) {
                cause = ex.getCause();
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw ex;
        }
    }

    public List<HandlerConfig> getHandlers() {
        return handlers;
    }

    protected static String[] resolveParameterNames(Method method) {
        MethodParameter[] params = Paramo.resolveParameters(method);
        String[] names = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            names[i] = params[i].getName();
        }
        return names;
    }

    public static class HandlerConfig implements Comparable<HandlerConfig> {

        public static HandlerConfig create(Object action, Method method, String macroString) {
            if (ClassUtil.isStatic(method)) {
                action = null;
            } else if (action == null) {
                throw new IllegalArgumentException("action is required to invoke member method: " + method);
            }
            ClassUtil.setAccessible(method);
            BasePathMacros macros = new RegExpPathMacros();
            boolean success = macros.init(macroString, MACRO_SEPARATORS);
            if (!success) {
                macros = null;
            }
            String[] names = macros == null
                    ? new String[0]
                    : macros.getNames();
            Class<?>[] paramTypes = method.getParameterTypes();
            String[] paramNames = resolveParameterNames(method);
            // assert paramNames.length == paramTypes.length
            int[] paramIndexer = new int[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                if (OutgoingMessage.class.isAssignableFrom(paramType)) {
                    paramIndexer[i] = -2;
                } else {
                    String paramName = paramNames[i];
                    int index = ArraysUtil.indexOf(names, paramName);
                    paramIndexer[i] = index >= 0 ? index : -1;
                }
            }
            return new HandlerConfig(action, method, macroString, macros, paramTypes, paramIndexer);
        }

        protected final Object action;
        protected final Method method;
        protected final String macrosString;
        protected final BasePathMacros macros;
        protected final String _head;  // for sort, high priority for longer

        protected final Class<?>[] paramTypes;
        // >0: macro values; -1: null; -2: OutgoingMessage
        protected final int[] paramIndexer;

        public HandlerConfig(Object action, Method method, String macrosString, BasePathMacros macros, Class<?>[] paramTypes, int[] paramIndexer) {
            this.action = action;
            this.method = method;
            this.macrosString = macrosString;
            this.macros = macros;
            this.paramTypes = paramTypes;
            this.paramIndexer = paramIndexer;
            this._head = StringUtil.cutTo(macrosString, MACRO_SEPARATORS[0]);
        }

        public boolean isMatch(String msg) {
            return macros == null ? macrosString.equals(msg) : macros.match(msg) >= 0;
        }

        public boolean hasMacros() {
            return macros != null;
        }

        public Object invoke(OutgoingMessage msg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            String text = msg.getFixedText();
            String[] values = macros != null
                    ? macros.exactExtract(text)
                    : null;

            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < args.length; i++) {
                int index = paramIndexer[i];
                switch (index) {
                    case -1:
                        args[i] = null;
                        break;
                    case -2:
                        args[i] = msg;
                        break;
                    default:
                        if (index >= 0) {
                            args[i] = Convert.convert(values[index], paramTypes[i]);
                            break;
                        }
                        throw new IllegalStateException("Unsupported index: " + index);
                }
            }
            return method.invoke(action, args);
        }

        public Method getMethod() {
            return method;
        }

        public String getMacrosString() {
            return macrosString;
        }

        public BasePathMacros getMacros() {
            return macros;
        }

        @Override
        public int compareTo(HandlerConfig o) {
            //Note: desc
            return o._head.compareTo(this._head);
        }

        @Override
        public String toString() {
            return '[' + macrosString + ']';
        }

    }
}
