/**
 * Copyright 2013 febit.org (support@febit.org)
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jodd.bean.BeanTemplateParser;
import jodd.io.FileNameUtil;
import jodd.io.findfile.ClassFinder;
import jodd.io.findfile.ClassScanner;
import org.febit.lang.Defaults;
import org.febit.lang.IdentitySet;
import org.febit.service.Services;
import org.febit.util.ArraysUtil;
import org.febit.util.ClassUtil;
import org.febit.util.CollectionUtil;
import org.febit.util.Petite;
import org.febit.util.StringUtil;
import org.febit.web.ActionConfig;
import org.febit.web.ActionRequest;
import org.febit.web.Filters;
import org.febit.web.RenderedFilter;
import org.febit.web.argument.Argument;
import org.febit.web.meta.Action;
import org.febit.web.meta.Filter;
import org.febit.web.meta.In;
import org.febit.web.upload.MultipartRequestWrapper;
import org.febit.web.upload.UploadFileFactory;
import org.febit.web.util.ServletUtil;
import org.febit.web.util.Wildcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zqq90
 */
public class ActionManager implements Component {

    protected static final Logger log = LoggerFactory.getLogger(ActionManager.class);
    protected static final BeanTemplateParser beanTemplateParser = new BeanTemplateParser();

    protected final Map<String, ActionConfig> actionConfigMap = new HashMap<>();
    protected final Map<String, List<String>> actionMatchMap = new HashMap<>();
    protected final Map<String, String[]> pathTokens = new HashMap<>();

    protected final IdentitySet<Class> discardCache = new IdentitySet<>(16);

    protected UploadFileFactory uploadFileFactory;
    protected Wrapper renderWrapper;
    protected Wrapper actionInvoker;
    protected String[] defaultFilters;
    protected String[] scan;

    protected Class[] discards;

    protected Petite petite;
    protected ArgumentManager argumentManager;

    protected int _nextId = 0;
    protected String[] _basePkgs;
    protected String[] _basePkgPaths;
    protected Wrapper[] _defaultWrappers;

    @Petite.Init
    public void init() {
        if (discards != null) {
            this.discardCache.addAll(discards);
        }
        initBasePaths();
        initDefaultWrappers();
    }

    public Collection<ActionConfig> getAllActionConfigs() {
        return Collections.unmodifiableCollection(actionConfigMap.values());
    }

    public int getActionCount() {
        return actionConfigMap.size();
    }

    public int getActionId(String path) {
        ActionConfig config = this.actionConfigMap.get(path);
        return config != null ? config.id : -1;
    }

    protected void initDefaultWrappers() {
        List<Wrapper> wrappers = new ArrayList<>();
        wrappers.add(renderWrapper);
        resolveWrappers(wrappers, defaultFilters);
        wrappers.add(actionInvoker);
        _defaultWrappers = sort(wrappers.toArray(new Wrapper[wrappers.size()]));
    }

    protected void initBasePaths() {
        final Map<String, String> map = CollectionUtil.createMap(this.scan.length);
        for (String raw : this.scan) {
            int index = raw.indexOf(' ');
            if (index < 0) {
                map.put(raw, "/");
            } else {
                map.put(raw.substring(index).trim() + '.', raw.substring(0, index));
            }
        }
        final int size = map.size();
        final String[] pkgs = this._basePkgs = map.keySet().toArray(new String[size]);
        Arrays.sort(pkgs);
        ArraysUtil.invert(pkgs);
        final String[] paths = this._basePkgPaths = new String[size];
        for (int i = 0; i < size; i++) {
            paths[i] = map.get(pkgs[i]);
        }
    }

    protected synchronized int nextId() {
        return _nextId++;
    }

    public ActionRequest buildActionRequest(HttpServletRequest request, HttpServletResponse response) {
        final ActionConfig actionConfig = actionConfigMap.get(ServletUtil.getRequestPath(request));
        if (actionConfig != null) {
            if (ServletUtil.isMultipartRequest(request)) {
                MultipartRequestWrapper wrapper = new MultipartRequestWrapper(request, uploadFileFactory);
                try {
                    wrapper.parseRequestStream("UTF-8");
                    request = wrapper;
                } catch (IOException ignore) {
                }
            }
            return new ActionRequest(actionConfig, request, response);
        }
        return null;
    }

    public void scanActions() {

        final ClassScanner scanner = new ClassScanner() {

            @Override
            protected void onEntry(ClassFinder.EntryData ed) throws Exception {
                String className = ed.getName();
                if (!className.endsWith("Action")) {
                    return;
                }
                Class actionClass = ClassUtil.getClass(className);
                if (discardCache.contains(actionClass)) {
                    return;
                }
                Annotation actionAnno = actionClass.getAnnotation(Action.class);
                if (actionAnno != null) {
                    ActionManager.this.register(actionClass);
                }
            }
        };
        String[] basePkgs = this._basePkgs;
        String[] includes = new String[basePkgs.length];
        for (int i = 0; i < basePkgs.length; i++) {
            includes[i] = basePkgs[i] + '*';
        }
        scanner.setExcludeAllEntries(true);
        scanner.setIncludeResources(false);
        scanner.setIncludedEntries(includes);
        scanner.scanDefaultClasspath();
    }

    public void register(Class type) {
        register(createActionInstance(type), false);
    }

    public void replace(Object action) {
        register(action, true);
    }

    public void register(Object action, boolean replace) {
        final Class type = action.getClass();
        for (Method method : type.getMethods()) {
            Action actionAnno = method.getAnnotation(Action.class);
            if (actionAnno != null) {
                register(createActionConfig(action, method));
            }
        }
    }

    protected void putToMatchMap(String key, String actionConfig) {
        List<String> group = this.actionMatchMap.get(key);
        if (group == null) {
            group = new ArrayList<>();
            this.actionMatchMap.put(key, group);
        }
        group.add(actionConfig);
    }

    protected void register(ActionConfig actionConfig) {
        register(actionConfig, false);
    }

    /**
     * 非线程安全
     *
     * @param actionConfig
     * @param replace
     */
    protected void register(ActionConfig actionConfig, boolean replace) {
        ActionConfig old = actionConfigMap.put(actionConfig.path, actionConfig);
        if (old == null) {
            final String path = actionConfig.path;
            int index = 0;
            for (;;) {
                index = path.indexOf('/', index + 1);
                if (index <= 0) {
                    break;
                }
                putToMatchMap(path.substring(0, index) + '*', path);
            }
            putToMatchMap("*", path);
            putToMatchMap(path, path);
            pathTokens.put(path, Wildcard.splitcPathPattern(path));
        } else if (!replace) {
            throw new RuntimeException("Duplicate action path '" + actionConfig.method + "' vs.'" + old.method + "'");
        }
    }

    public List<String> getMatchPaths(String key) {
        if (key.charAt(0) == '@') {
            String[] pattern = Wildcard.splitcPathPattern(key.substring(1));
            List<String> result = new ArrayList<>();
            for (Map.Entry<String, String[]> entrySet : this.pathTokens.entrySet()) {
                if (Wildcard.matchPathTokens(entrySet.getValue(), pattern)) {
                    result.add(entrySet.getKey());
                }
            }
            return result;
        } else {
            return this.actionMatchMap.get(key);
        }
    }

    protected ActionConfig createActionConfig(final Object action, final Method method) {

        Class[] argTypes = method.getParameterTypes();

        final int argLen = argTypes.length;
        final Argument[] arguments;
        final String[] argNames;
        if (argLen != 0) {
            argNames = new String[argLen];
            arguments = new Argument[argLen];
            final Annotation[][] annotationses = method.getParameterAnnotations();
            for (int i = 0; i < argLen; i++) {
                Annotation[] annotations = annotationses[i];
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == In.class) {
                        String name = ((In) annotation).value();
                        if (name != null && name.length() != 0) {
                            argNames[i] = name;
                        }
                    }
                }
                arguments[i] = argumentManager.resolveArgument(argTypes[i], argNames[i], i);
            }
        } else {
            argTypes = Defaults.EMPTY_CLASSES;
            argNames = Defaults.EMPTY_STRINGS;
            arguments = null;
        }

        String path = resolvePath(action, method);
        Wrapper[] wrappers = resolveWrappers(action, method);
        return new ActionConfig(nextId(), action, method, path, argTypes, argNames, arguments, wrappers);
    }

    protected Wrapper[] sort(final Wrapper[] wrappers) {
        final int len = wrappers.length;
        final Wrapper[] newWrappers = new Wrapper[len];

        int curr = 0;
        for (int i = 0; i < len; i++) {
            if (wrappers[i] instanceof RenderedFilter) {
                newWrappers[curr++] = wrappers[i];
                wrappers[i] = null;
            }
        }
        if (curr == 0) {
            return wrappers;
        }
        for (int i = 0; i < len; i++) {
            Wrapper wrapper = wrappers[i];
            if (wrapper != null) {
                newWrappers[curr++] = wrapper;
            }
        }
        return newWrappers;
    }

    protected void resolveWrappers(List<Wrapper> list, String... names) {
        if (names == null) {
            return;
        }
        for (String name : names) {
            if (name.charAt(0) == '#') {
                continue;
            }
            Object comp = petite.get(name);
            if (comp == null) {
                throw new RuntimeException("No found Wrapper named: " + name);
            }
            if (comp instanceof Wrapper) {
                list.add((Wrapper) comp);
                continue;
            }
            if (comp instanceof Filters) {
                resolveWrappers(list, ((Filters) comp).filters);
                continue;
            }
            throw new RuntimeException("Need a Wrapper(s), but get a " + comp.getClass());
        }
    }

    protected Wrapper[] resolveWrappers(final Object action, final Method method) {
        Filter filterBy = method.getAnnotation(Filter.class);
        if (filterBy == null) {
            return this._defaultWrappers;
        }
        final List<Wrapper> wrappers = new ArrayList<>();
        wrappers.add(renderWrapper);

        for (String name : filterBy.value()) {
            resolveWrappers(wrappers, name);
        }

        wrappers.add(actionInvoker);
        return sort(wrappers.toArray(new Wrapper[wrappers.size()]));
    }

    protected String resolvePath(final Object action, final Method method) {

        final Class type = action.getClass();

        //method path
        Action methodAnno = method.getAnnotation(Action.class);
        String path = methodAnno.value();
        if (path == null || path.isEmpty()) {
            path = method.getName();
            if (path.equals("execute")) {
                path = null;
            }
        }

        if (path == null || path.charAt(0) != '/') {
            //append class path
            Action actionAnno = (Action) type.getAnnotation(Action.class);
            String actionClassPath = actionAnno.value();
            if (actionClassPath == null || actionClassPath.isEmpty()) {
                actionClassPath = StringUtil.lowerFirst(StringUtil.cutSuffix(type.getSimpleName(), "Action"));
            }
            if (path == null) {
                path = actionClassPath;
            } else {
                path = FileNameUtil.concat(actionClassPath, path, true);
            }
        }

        if (path.charAt(0) != '/') {
            // append packagePath
            String pkg = type.getPackage().getName() + '.';
            for (int i = 0; i < _basePkgs.length; i++) {
                String basePkg = _basePkgs[i];
                if (pkg.startsWith(basePkg)) {
                    String pkgPath = FileNameUtil.concat(_basePkgPaths[i], StringUtil.replaceChar(pkg.substring(basePkg.length()), '.', '/'), true);
                    path = FileNameUtil.concat(pkgPath, path, true);
                    break;
                }
            }
        }

        if (path.charAt(0) != '/') {
            //fix path
            path = '/' + path;
        }
        if (path.indexOf("${") > 0) {
            path = beanTemplateParser.parse(path, action);
        }
        if (log.isDebugEnabled()) {
            log.debug("MAPPED: {}#{} -> {}", type, method.getName(), path);
        }
        return path;
    }

    protected Object createActionInstance(Class type) {
        final Object action = ClassUtil.newInstance(type);
        Services.inject(action);
        return action;
    }
}
