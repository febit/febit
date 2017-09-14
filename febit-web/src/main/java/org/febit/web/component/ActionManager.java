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
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jodd.bean.BeanTemplateParser;
import jodd.io.FileNameUtil;
import jodd.io.findfile.ClassFinder;
import jodd.io.findfile.ClassScanner;
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
import org.febit.web.argument.Argument;
import org.febit.web.argument.ArgumentConfig;
import org.febit.web.meta.Action;
import org.febit.web.meta.Filter;
import org.febit.web.meta.In;
import org.febit.web.upload.MultipartRequestWrapper;
import org.febit.web.upload.UploadFileFactory;
import org.febit.web.util.ServletUtil;
import org.febit.web.util.Wildcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.febit.web.OuterFilter;

/**
 *
 * @author zqq90
 */
public class ActionManager implements Component {

    protected static final Logger LOG = LoggerFactory.getLogger(ActionManager.class);
    protected static final BeanTemplateParser PATH_TEMPLATE_PARSER = new BeanTemplateParser();
    protected static final ArgumentConfig[] EMPTY_ARGS = new ArgumentConfig[0];

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

    protected AtomicInteger _nextId = new AtomicInteger(0);
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
        final String[] pkgs = map.keySet().toArray(new String[size]);
        Arrays.sort(pkgs);
        ArraysUtil.invert(pkgs);
        this._basePkgs = pkgs;
        final String[] paths = new String[size];
        for (int i = 0; i < size; i++) {
            paths[i] = map.get(pkgs[i]);
        }
        this._basePkgPaths = paths;
    }

    public ActionRequest buildActionRequest(HttpServletRequest request, HttpServletResponse response) {
        String path = ServletUtil.getRequestPath(request);
        return buildActionRequest(path, request, response);
    }

    /**
     * Build ActionRequest for a http request.
     *
     * @param path
     * @param request
     * @param response
     * @return if not found will returns null.
     */
    public ActionRequest buildActionRequest(String path, HttpServletRequest request, HttpServletResponse response) {
        final ActionConfig actionConfig = actionConfigMap.get(path);
        if (actionConfig == null) {
            return null;
        }
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

    public void scanActions() {
        final List<Class> actionClasses = new ArrayList<>();
        final ClassScanner scanner = new ClassScanner() {

            @Override
            protected void onEntry(ClassFinder.EntryData ed) throws Exception {
                String className = ed.getName();
                if (!className.endsWith("Action")) {
                    return;
                }
                Class actionClass = ClassUtil.getClass(className);
                if (ClassUtil.isAbstract(actionClass)) {
                    return;
                }
                if (discardCache.contains(actionClass)) {
                    return;
                }
                Annotation actionAnno = actionClass.getAnnotation(Action.class);
                if (actionAnno == null) {
                    return;
                }
                LOG.debug("Find action: {}", actionClass);
                actionClasses.add(actionClass);
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

        // register actions
        for (Class actionClass : actionClasses) {
            register(actionClass);
        }
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
            if (actionAnno == null) {
                continue;
            }
            register(createActionConfig(action, method), replace);
        }
    }

    protected void registerToMatchMap(String path) {
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
    }

    protected void putToMatchMap(String key, String fullPath) {
        List<String> group = this.actionMatchMap.get(key);
        if (group == null) {
            group = new ArrayList<>();
            this.actionMatchMap.put(key, group);
        }
        group.add(fullPath);
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
        if (old != null && !replace) {
            throw new RuntimeException("Duplicate action path '" + actionConfig.method + "' vs.'" + old.method + "'");
        }
        final String path = actionConfig.path;
        registerToMatchMap(path);
        pathTokens.put(path, Wildcard.splitcPathPattern(path));
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
        String path = resolvePath(action, method);
        Wrapper[] wrappers = resolveWrappers(action, method);
        ArgumentConfig[] arguments = resolveArgumentConfigs(action, method);
        return new ActionConfig(_nextId.incrementAndGet(), action, method, path, arguments, wrappers);
    }

    protected ArgumentConfig[] resolveArgumentConfigs(final Object action, final Method method) {
        final Class[] argTypes = method.getParameterTypes();
        final int argLen = argTypes.length;
        if (argLen == 0) {
            return EMPTY_ARGS;
        }
        final ArgumentConfig[] arguments = new ArgumentConfig[argLen];
        final Annotation[][] annotationses = method.getParameterAnnotations();
        for (int i = 0; i < argLen; i++) {
            String name = null;
            Class<?> argType = argTypes[i];
            for (Annotation annotation : annotationses[i]) {
                if (annotation.annotationType() == In.class) {
                    name = ((In) annotation).value();
                    break;
                }
            }
            if (name != null && name.isEmpty()) {
                name = null;
            }
            Argument argument = argumentManager.resolveArgument(argType, name, i);
            arguments[i] = new ArgumentConfig(i, name, argType, argument);
        }
        return arguments;
    }

    protected Wrapper[] sort(final Wrapper[] wrappers) {
        final int len = wrappers.length;
        final Wrapper[] sorted = new Wrapper[len];

        int curr = 0;
        for (int i = 0; i < len; i++) {
            if (wrappers[i] instanceof OuterFilter) {
                sorted[curr++] = wrappers[i];
                wrappers[i] = null;
            }
        }
        if (curr == 0) {
            // without changes
            return wrappers;
        }
        for (Wrapper wrapper : wrappers) {
            if (wrapper != null) {
                sorted[curr++] = wrapper;
            }
        }
        return sorted;
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

        final Class<?> type = action.getClass();

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
            Action actionAnno = type.getAnnotation(Action.class);
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
            path = PATH_TEMPLATE_PARSER.parse(path, action);
        }
        LOG.debug("Action: {}#{} -> {}", type, method.getName(), path);
        return path;
    }

    protected Object createActionInstance(Class type) {
        LOG.debug("Creating action {} ...", type);
        final Object action = ClassUtil.newInstance(type);
        Services.inject(action);
        return action;
    }
}
