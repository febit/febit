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
package org.febit.web.util;

import java.util.Locale;
import jodd.util.LocaleUtil;
import jodd.util.ResourceBundleMessageResolver;
import org.febit.web.ActionRequest;

/**
 * TODO Finds i18n text.
 */
public class I18nUtil {

    public static final ResourceBundleMessageResolver MESSAGE_RESOLVER = new ResourceBundleMessageResolver();
    public static final String KEY_LOCALE = I18nUtil.class.getName() + ".locale";

    static {
        MESSAGE_RESOLVER.addDefaultBundle("messages");
        MESSAGE_RESOLVER.addDefaultBundle("validation");
    }

    public static void setLocale(ActionRequest actionRequest, String localeCode) {
        actionRequest.request.getSession().setAttribute(KEY_LOCALE, LocaleUtil.getLocale(localeCode));
    }

    public static Locale getLocale(ActionRequest actionRequest) {
        return (Locale) actionRequest.request.getSession().getAttribute(KEY_LOCALE);
    }

    public static String getBundleName(ActionRequest actionRequest) {
        return actionRequest.actionConfig.action.getClass().getName();
    }

    public static String findMessage(ActionRequest actionRequest, String key) {
        return findMessage(getBundleName(actionRequest), getLocale(actionRequest), key);
    }

    public static String findMessage(String bundleName, Locale locale, String key) {
        return MESSAGE_RESOLVER.findMessage(bundleName, locale, key);
    }
}
