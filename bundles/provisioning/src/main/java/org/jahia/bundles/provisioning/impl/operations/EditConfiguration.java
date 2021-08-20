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
package org.jahia.bundles.provisioning.impl.operations;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bundles.config.OsgiConfigService;
import org.jahia.bundles.config.Format;
import org.jahia.services.modulemanager.spi.Config;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Edit configuration operation
 */
@Component(service = Operation.class, property = "type=editConfiguration")
public class EditConfiguration implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(EditConfiguration.class);
    public static final String EDIT_CONFIGURATION = "editConfiguration";
    public static final String INSTALL_CONFIGURATION = "installConfiguration";
    public static final String CONFIG_IDENTIFIER = "configIdentifier";
    public static final String PROPERTIES = "properties";
    public static final String CONTENT = "content";

    private OsgiConfigService configService;

    @Reference
    protected void setConfigService(OsgiConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.get(EDIT_CONFIGURATION) instanceof String || entry.get(INSTALL_CONFIGURATION) instanceof String;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            String pid = (String) entry.get(EDIT_CONFIGURATION);
            String configId = (String) entry.get(CONFIG_IDENTIFIER);
            String content = (String) entry.get(CONTENT);
            Map<String, String> properties = (Map<String, String>) entry.get(PROPERTIES);
            Format format = Format.CFG;
            String installConfiguration = (String) entry.get(INSTALL_CONFIGURATION);
            if (installConfiguration != null) {
                String filename = installConfiguration.contains("/") ? StringUtils.substringAfterLast(installConfiguration, "/") : installConfiguration;
                String extension = FilenameUtils.getExtension(filename);
                if(Format.YAML.getSupportedExtensions().contains("."+extension)) {
                    format = Format.YAML;
                }
                pid = FilenameUtils.getBaseName(filename);
                content = IOUtils.toString(ProvisioningScriptUtil.getResource(installConfiguration, executionContext).getURL(), StandardCharsets.UTF_8);
            }

            if (pid.contains("-")) {
                configId = StringUtils.substringAfter(pid, "-");
                pid = StringUtils.substringBefore(pid, "-");
            }

            Config settings = configId != null ? configService.getConfig(pid, configId) : configService.getConfig(pid);

            if (content != null) {
                settings.setFormat(format.toString());
                settings.setContent(content);
            }
            if (properties != null) {
                settings.getRawProperties().putAll(properties);
            }

            configService.storeConfig(settings);
        } catch (IOException e) {
            logger.error("Cannot update configurations", e);
        }
    }
}
