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
import jodd.util.StringTemplateParser;
import org.febit.lang.IdentitySet;
import org.febit.lang.Tuple2;
import org.febit.service.Services;
import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.StringUtil;
import org.febit.web.ActionConfig;
import org.febit.web.ActionRequest;
import org.febit.web.Filters;
import org.febit.web.OuterFilter;
import org.febit.web.argument.Argument;
import org.febit.web.argument.ArgumentConfig;
import org.febit.web.meta.Action;
import org.febit.web.meta.In;
import org.febit.web.upload.MultipartRequestWrapper;
import org.febit.web.upload.UploadFileFactory;
import org.febit.web.util.ActionMacroPath;
import org.febit.web.util.AnnotationUtil;
import org.febit.web.util.ServletUtil;
import org.febit.web.util.Wildcard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zqq90
 */
public class ActionManager implements Component {

    protected static final Logger LOG = LoggerFactory.getLogger(ActionManager.class);
    protected static final StringTemplateParser PATH_TEMPLATE_PARSER = new StringTemplateParser().setMacroPrefix("${");
    protected static final ArgumentConfig[] EMPTY_ARGS = new ArgumentConfig[0];
    protected static final List<String> DEFAULT_HTTP_METHODS = Collections.unmodifiableList(Arrays.asList("GET", "POST"));

    protected final Map<String, ActionConfig> actionConfigMap = new HashMap<>();
    protected final Map<String, List<String>> actionMatchMap = new HashMap<>();
    protected final Map<String, String[]> pathTokens = new HashMap<>();
    protected final ActionMacroPath.Parser actionMacroPathParser = ActionMacroPath.newParser();

    protected final AtomicInteger _nextId = new AtomicInteger(0);
    protected final IdentitySet<Class> _discardCaching = new IdentitySet<>(16);

    protected UploadFileFactory uploadFileFactory;
    protected Wrapper renderWrapper;
    protected Wrapper actionInvoker;
    protected String[] defaultFilters;
    protected String[] scan;
    protected Class[] discards;

    protected Petite petite;
    protected ArgumentManager argumentManager;

    protected List<Tuple2<String, String>> _basePkgs;
    protected Wrapper[] _defaultWrappers;

    private transient Tuple2<Class, String> _pathPrefixCaching = null;

    @Petite.Init
    public void init() {
        if (discards != null) {
            this._discardCaching.addAll(discards);
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

    public int getActionId(String key) {
        ActionConfig config = this.actionConfigMap.get(key);
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
        List<Tuple2<String, String>> list = new ArrayList<>();
        for (String raw : this.scan) {
            int index = raw.indexOf(' ');
            if (index < 0) {
                list.add(Tuple2.of(raw, "/"));
            } else {
                list.add(Tuple2.of(
                        raw.substring(index).trim() + '.',
                        raw.substring(0, index)
                ));
            }
        }
        Collections.sort(list, (o1, o2) -> {
            // _1 DESC 
            return o2._1.compareTo(o1._1);
        });
        this._basePkgs = Collections.unmodifiableList(list);
    }

    public ActionRequest buildActionRequest(HttpServletRequest request, HttpServletResponse response) {
        String path = ServletUtil.getRequestPath(request);
        return buildActionRequest(request.getMethod(), path, request, response);
    }

    /**
     * Build ActionRequest for a http request.
     *
     * @param method
     * @param path
     * @param request
     * @param response
     * @return if not found will returns null.
     */
    public ActionRequest buildActionRequest(String method, String path, HttpServletRequest request, HttpServletResponse response) {
        String key = buildPathKey(method, path);
        ActionMacroPath macroPath = actionMacroPathParser.parse(key);
        ActionConfig actionConfig = actionConfigMap.get(macroPath.key);
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
        return new ActionRequest(actionConfig, request, response, macroPath.params);
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
                if (ClassUtil.isAbstract(actionClass)
                        || _discardCaching.contains(actionClass)
                        || !AnnotationUtil.isAction(actionClass)) {
                    return;
                }
                LOG.debug("Find action: {}", actionClass);
                actionClasses.add(actionClass);
            }
        };
        String[] includes = new String[_basePkgs.size()];
        for (int i = 0; i < _basePkgs.size(); i++) {
            includes[i] = _basePkgs.get(i)._1 + '*';
        }
        scanner.setExcludeAllEntries(true);
        scanner.setIncludeResources(false);
        scanner.setIncludedEntries(includes);
        scanner.scanDefaultClasspath();

        // register actions
        actionClasses.forEach(this::register);
    }

    public void register(Class<?> type) {
        register(createActionInstance(type), false);
    }

    public void replace(Object action) {
        register(action, true);
    }

    public void register(Object action, boolean replace) {
        final Class type = action.getClass();
        for (Method method : type.getMethods()) {
            if (!AnnotationUtil.isAction(method)) {
                continue;
            }
            register(createActionConfig(action, method), replace);
        }
    }

    protected void registerToMatchMap(String key) {
        int index = 0;
        for (;;) {
            index = key.indexOf('/', index + 1);
            if (index <= 0) {
                break;
            }
            putToMatchMap(key.substring(0, index) + '*', key);
        }
        putToMatchMap("*", key);
        putToMatchMap(key, key);
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

    protected void register(ActionConfig[] actionConfigs, boolean replace) {
        for (ActionConfig actionConfig : actionConfigs) {
            register(actionConfig, replace);
        }
    }

    protected void register(ActionConfig actionConfig, boolean replace) {
        final String key = buildPathKey(actionConfig.httpMethod, actionConfig.path);
        ActionConfig old = actionConfigMap.put(key, actionConfig);
        if (old != null && !replace) {
            throw new RuntimeException("Duplicate action path '" + actionConfig.handler + "' vs.'" + old.handler + "'");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Action: {}#{} -> {}: {}",
                    actionConfig.action.getClass(), actionConfig.handler.getName(), actionConfig.httpMethod, actionConfig.path);
        }
        actionMacroPathParser.add(key);
        registerToMatchMap(key);
        pathTokens.put(key, Wildcard.splitcPathPattern(key));
    }

    public List<String> getMatchPaths(String key) {
        if (key.charAt(0) != '@') {
            return this.actionMatchMap.get(key);
        }
        String[] pattern = Wildcard.splitcPathPattern(key.substring(1));
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String[]> entrySet : this.pathTokens.entrySet()) {
            if (Wildcard.matchPathTokens(entrySet.getValue(), pattern)) {
                result.add(entrySet.getKey());
            }
        }
        return result;
    }

    protected ActionConfig[] createActionConfig(final Object action, final Method method) {
        String path = resolvePath(action, method);
        Wrapper[] wrappers = resolveWrappers(action, method);
        ArgumentConfig[] arguments = resolveArgumentConfigs(action, method);
        List<String> httpMethods = AnnotationUtil.getHttpMethods(method);
        if (httpMethods.isEmpty()) {
            httpMethods = DEFAULT_HTTP_METHODS;
        }
        ActionConfig[] configs = new ActionConfig[httpMethods.size()];
        for (int i = 0; i < httpMethods.size(); i++) {
            configs[i] = new ActionConfig(_nextId.incrementAndGet(), action, method, path, httpMethods.get(i), arguments, wrappers);
        }
        return configs;
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
            Class<? extends ArgumentResolver> argumentResolverClass = AnnotationUtil.getArgumentResolverClass(annotationses[i]);
            Argument argument = (argumentResolverClass == null ? argumentManager : petite.get(argumentResolverClass))
                    .resolveArgument(argType, name, i);
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
        List<String> filters = AnnotationUtil.getFilters(method);
        if (filters == null) {
            return this._defaultWrappers;
        }
        final List<Wrapper> wrappers = new ArrayList<>();
        wrappers.add(renderWrapper);
        filters.forEach((filter) -> {
            resolveWrappers(wrappers, filter);
        });
        wrappers.add(actionInvoker);
        return sort(wrappers.toArray(new Wrapper[wrappers.size()]));
    }

    protected String resolvePackageActionPath(Class actionType) {
        String pkg = actionType.getPackage().getName() + '.';
        for (Tuple2<String, String> basePkg : this._basePkgs) {
            if (!pkg.startsWith(basePkg._1)) {
                continue;
            }
            String path = StringUtil.replaceChar(pkg.substring(basePkg._1.length()), '.', '/');
            path = FileNameUtil.concat(basePkg._2, path, true);
            if (path == null || path.isEmpty() || "/".equals(path)) {
                return "";
            }
            return StringUtil.cutSurrounding(path, "/");
        }
        return "";
    }

    protected String resolveClassActionPath(Class<?> actionType) {
        String actionClassPath = actionType.getSimpleName();
        actionClassPath = StringUtil.cutSuffix(actionClassPath, "Action");
        actionClassPath = StringUtil.lowerFirst(actionClassPath);
        return actionClassPath;
    }

    protected String buildPathKey(String method, String path) {
        return method.toUpperCase() + ':' + path;
    }

    protected String resolvePathPrefix(final Class<?> actionClass) {
        Tuple2<Class, String> caching = this._pathPrefixCaching;
        if (caching != null && caching._1 == actionClass) {
            return caching._2;
        }
        Action actionAnno = actionClass.getAnnotation(Action.class);
        String classActionPath = actionAnno != null ? actionAnno.value() : null;
        if (classActionPath == null
                || classActionPath.isEmpty()) {
            classActionPath = resolveClassActionPath(actionClass);
        }
        classActionPath = resolveInternalPathMacro(classActionPath, "#CLASS");
        String packageActionPath = resolveInternalPathMacro(resolvePackageActionPath(actionClass), "#PACKAGE");
        String prefix = FileNameUtil.concat(packageActionPath, classActionPath, true);
        if (prefix == null) {
            prefix = "";
        }
        if (!prefix.isEmpty() && prefix.charAt(0) != '/') {
            prefix = '/' + prefix;
        }
        this._pathPrefixCaching = Tuple2.of(actionClass, prefix);
        return prefix;
    }

    protected String resolvePath(final Object action, final Method method) {

        // prefix path
        String prefix = resolvePathPrefix(action.getClass());

        //method path
        String path = AnnotationUtil.getActionAnnoValue(method);
        if (path == null) {
            path = "${#}";
        }
        path = resolveInternalPathMacro(path, "#METHOD");

        // prepend prefix
        if (path == null || path.isEmpty()) {
            path = prefix;
        } else {
            path = FileNameUtil.concat(prefix, path, true);
        }

        // resolve macro
        path = PATH_TEMPLATE_PARSER.parse(path, createActionPathMacroResolver(action, method));

        // resure start with slash
        if (path.charAt(0) != '/') {
            //fix path
            path = '/' + path;
        }
        return path;
    }

    protected String resolveInternalPathMacro(final String src, String current) {
        if (src == null) {
            return null;
        }
        return PATH_TEMPLATE_PARSER.parse(src, (String macroName) -> "${"
                + (macroName.equals("#") ? current : macroName)
                + "}");
    }

    protected StringTemplateParser.MacroResolver createActionPathMacroResolver(Object action, Method method) {
        StringTemplateParser.MacroResolver actionMacroResolver = BeanTemplateParser.createBeanMacroResolver(action);
        return (String macroName) -> {
            switch (macroName) {
                case "#METHOD":
                    return method.getName();
                case "#CLASS":
                    return resolveClassActionPath(action.getClass());
                case "#PACKAGE":
                    return resolvePackageActionPath(action.getClass());
                default:
                    return actionMacroResolver.resolve(macroName);
            }
        };
    }

    protected Object createActionInstance(Class<?> type) {
        LOG.debug("Creating action {} ...", type);
        final Object action = ClassUtil.newInstance(type);
        Services.inject(action);
        return action;
    }
}
