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
package org.jahia.bundles.provisioning.impl.operations;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enable module on site
 */
@Component(service = Operation.class, property = "type=enableOnSite")
public class EnableOnSite implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(EnableOnSite.class);
    public static final String ENABLE_ON_SITE = "enable";
    public static final String SITE = "site";

    private JCRTemplate jcrTemplate;
    private JahiaSitesService sitesService;
    private JahiaTemplateManagerService templateManagerService;

    @Reference
    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    @Reference
    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Reference
    public void setModuleManager(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(ENABLE_ON_SITE) && entry.containsKey(SITE);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, ENABLE_ON_SITE, "key");

        Map<?, List<String>> values = entries.stream()
                .collect(Collectors.groupingBy(m -> m.get(SITE), Collectors.mapping(m -> (String) m.get(ENABLE_ON_SITE), Collectors.toList())));

        try {
            jcrTemplate.doExecuteWithSystemSession(session -> {
                for (Map.Entry<?, List<String>> listEntry : values.entrySet()) {
                    List<String> sites = (listEntry.getKey() instanceof List) ? (List) listEntry.getKey() : Collections.singletonList((String) listEntry.getKey());
                    for (String siteName : sites) {
                        if (sitesService.siteExists(siteName, session)) {
                            JahiaSite site = sitesService.getSiteByKey(siteName, session);
                            List<JahiaTemplatesPackage> pkgs = listEntry.getValue().stream()
                                    .map(name -> StringUtils.substringBefore(name, "/"))
                                    .map(n -> templateManagerService.getTemplatePackageById(n))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            templateManagerService.installModules(pkgs, site.getJCRLocalPath(), session);
                            session.save();
                        } else {
                            logger.error("Site {} does not exist", siteName);
                        }
                    }
                }
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("Cannot enable modules", e);
        }
    }
}
