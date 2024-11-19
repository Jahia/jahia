/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.jahia.settings.SettingsBean;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.SystemPropertyUtils;

/**
 * Helper class for resolving placeholders in texts with values of DX settings.
 *
 * @author Sergiy Shyrkov
 */
public class PlaceholderUtils {

    private static class SettingsPlaceholderResolver implements PlaceholderResolver {

        private boolean fallbackToSystemProperties;

        private SettingsBean settingsBean;

        SettingsPlaceholderResolver(SettingsBean settingsBean, boolean fallbackToSystemProperties) {
            this.settingsBean = settingsBean;
            this.fallbackToSystemProperties = fallbackToSystemProperties;
        }

        @Override
        public String resolvePlaceholder(String placeholderName) {
            String value = settingsBean.getPropertiesFile().getProperty(placeholderName);
            if (value == null && fallbackToSystemProperties) {
                value = System.getProperty(placeholderName);
            }
            return value;
        }
    }

    private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER_NON_STRICT = new PropertyPlaceholderHelper(
            SystemPropertyUtils.PLACEHOLDER_PREFIX, SystemPropertyUtils.PLACEHOLDER_SUFFIX,
            SystemPropertyUtils.VALUE_SEPARATOR, true);

    public static final PropertyPlaceholderHelper PLACEHOLDER_HELPER_STRICT = new PropertyPlaceholderHelper(
            SystemPropertyUtils.PLACEHOLDER_PREFIX, SystemPropertyUtils.PLACEHOLDER_SUFFIX,
            SystemPropertyUtils.VALUE_SEPARATOR, false);;

    /**
     * Resolve {@code ${...}} placeholders in the given configuration resource stream, replacing them with corresponding values from DX
     * settings ({@link SettingsBean}) with a fallback to Java system properties. Unresolvable placeholders with no default value are
     * ignored and passed through unchanged if the flag is set to {@code true}.
     *
     * @param configInputStream the configuration resource input stream to relpace plpaceholders in
     * @param settingsBean the DX settings bean instance
     * @param ignoreUnresolvablePlaceholders whether unresolved placeholders are to be ignored
     * @return a new input stream of the configuration resource with placeholders replaced
     * @throws IOException in case of an I/O errors
     * @throws IllegalArgumentException if there is an unresolvable placeholder and the "ignoreUnresolvablePlaceholders" flag is
     *             {@code false}
     */
    public static InputStream resolvePlaceholders(InputStream configInputStream, SettingsBean settingsBean,
            boolean ignoreUnresolvablePlaceholders) throws IOException {
        return IOUtils.toInputStream(resolvePlaceholders(IOUtils.toString(configInputStream, Charsets.UTF_8),
                settingsBean, ignoreUnresolvablePlaceholders), Charsets.UTF_8);
    }

    /**
     * Resolve {@code ${...}} placeholders in the given text, replacing them with corresponding values from DX settings
     * ({@link SettingsBean}) with a fallback to Java system properties. Unresolvable placeholders with no default value are ignored and
     * passed through unchanged if the flag is set to {@code true}.
     *
     * @param text the String to resolve
     * @param settingsBean the DX settings bean instance
     * @param ignoreUnresolvablePlaceholders whether unresolved placeholders are to be ignored
     * @return the resolved String
     * @throws IllegalArgumentException if there is an unresolvable placeholder and the "ignoreUnresolvablePlaceholders" flag is
     *             {@code false}
     */
    public static String resolvePlaceholders(String text, SettingsBean settingsBean,
            boolean ignoreUnresolvablePlaceholders) {
        return (ignoreUnresolvablePlaceholders ? PLACEHOLDER_HELPER_NON_STRICT : PLACEHOLDER_HELPER_STRICT)
                .replacePlaceholders(text, new SettingsPlaceholderResolver(settingsBean, true));
    }

}
