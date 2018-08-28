/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.settings;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the startup options, which are "instructions" to DX about actions to be performed on startup. The options are determined,
 * based on so called marker files on file system.
 *
 * @author Sergiy Shyrkov
 */
public class StartupOptions {

    private static final Logger logger = LoggerFactory.getLogger(StartupOptions.class);

    public static final String OPTION_DISABLE_MAIL_SERVICE = "disable-mail-service";
    public static final String OPTION_INDEX_CHECK = "index-check";
    public static final String OPTION_INDEX_FIX = "index-fix";
    public static final String OPTION_REINDEX = "reindex";
    public static final String OPTION_RESET_DISCOVERY_INFO = "reset-discovery-info";

    private static boolean deleteMarkerIfPresent(String markerPath, SettingsBean settings) throws IOException {
        File marker = new File(interpolate(markerPath, settings));
        boolean present = marker.exists();
        if (present) {
            FileUtils.deleteQuietly(marker);
        }

        return present;
    }

    private static String interpolate(String marker, SettingsBean settings) throws IOException {
        if (marker.indexOf("#jahia.data.dir#") != -1) {
            return StringUtils.replace(marker, "#jahia.data.dir#", settings.getJahiaVarDiskPath());
        } else if (marker.indexOf("#jahia.jackrabbit.home#") != -1) {
            return StringUtils.replace(marker, "#jahia.jackrabbit.home#", settings.getRepositoryHome().getPath());
        }
        return marker;
    }

    private Set<String> options = Collections.emptySet();

    StartupOptions(SettingsBean settings, Map<String, Set<String>> mapping) {
        super();
        init(settings, mapping);
    }

    /**
     * Returns the startup options, which are set.
     *
     * @return the startup options, which are set
     */
    public Set<String> getOptions() {
        return options;
    }

    @SuppressWarnings("deprecation")
    private void init(SettingsBean settings, Map<String, Set<String>> mapping) {
        Set<String> opts = new HashSet<>();
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing startup options using mapping: {}", mapping);
        }
        try {
            for (Map.Entry<String, Set<String>> mappingEntry : mapping.entrySet()) {
                if (deleteMarkerIfPresent(mappingEntry.getKey(), settings)) {
                    for (String option : mappingEntry.getValue()) {
                        opts.add(option);
                    }
                    if (mappingEntry.getKey().endsWith("/backup-restore")
                            || mappingEntry.getKey().endsWith("/safe-env-clone")) {
                        // support deprecated system property, if someone still relies on it
                        JahiaContextLoaderListener.setSystemProperty(SettingsBean.JAHIA_BACKUP_RESTORE_SYSTEM_PROP,
                                "true");
                    }
                }
            }
            options = Collections.unmodifiableSet(opts);
            logger.info("Initialized startup options: {}", options);
        } catch (IOException e) {
            logger.error("Unable to initialize startup options", e);
        }
    }

    /**
     * Checks if the specified startup option is set.
     *
     * @param option the option key
     * @return <code>true</code> if the specified option is set; <code>false</code> otherwise
     */
    public boolean isSet(String option) {
        return options.contains(option);
    }
}