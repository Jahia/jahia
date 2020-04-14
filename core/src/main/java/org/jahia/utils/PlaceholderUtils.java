/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
