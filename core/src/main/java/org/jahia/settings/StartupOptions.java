/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
        logger.debug("Initializing startup options using mapping: {}", mapping);
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