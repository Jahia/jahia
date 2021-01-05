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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return entry.get(ENABLE_ON_SITE) instanceof String && entry.get(SITE) instanceof String;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        String[] moduleName = StringUtils.split((String) entry.get(ENABLE_ON_SITE), " ,");
        String siteName = (String) entry.get(SITE);
        try {
            jcrTemplate.doExecuteWithSystemSession(session -> {
                JahiaSite site = sitesService.getSiteByKey(siteName, session);
                if (site != null) {
                    List<JahiaTemplatesPackage> pkgs = Arrays.stream(moduleName).map(name -> StringUtils.substringBefore(name, "/"))
                            .map(n -> templateManagerService.getTemplatePackageById(n))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    templateManagerService.installModules(pkgs, site.getJCRLocalPath(), session);
                    session.save();
                } else {
                    logger.error("Site {} does not exist", siteName);
                }
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("Cannot enable module on site {}", siteName, e);
        }
    }
}
