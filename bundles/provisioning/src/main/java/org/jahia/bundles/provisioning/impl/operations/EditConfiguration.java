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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
