/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * Create site operation
 */
@Component(service = Operation.class, property = "type=createSite")
public class CreateSite implements Operation {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateSite.class);
    public static final String CREATE_SITE = "createSite";

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(CREATE_SITE);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        try {
            final SiteCreationInfo siteInfo = getSiteInfo(entry);
            createSite(siteInfo);
        } catch (Exception e) {
            LOGGER.error("Unable to read site info", e);
        }
    }

    private void createSite(SiteCreationInfo siteInfo) {
        try {
            JahiaSitesService.getInstance().addSite(siteInfo);
        } catch (Exception e) {
            LOGGER.error("Unable to create site from info {}", siteInfo);
            throw new JahiaRuntimeException(e);
        }
    }

    private SiteCreationInfo getSiteInfo(Map<String, Object> entry) {
        SiteCreationInfo siteInfo = SiteCreationInfo.builder().
                siteKey((String) entry.get("siteKey")).
                templateSet((String) entry.get("templateSet")).
                build();
        if (entry.get("modulesToDeploy") != null) {
            siteInfo.setModulesToDeploy((String[]) ((ArrayList) entry.get("modulesToDeploy")).toArray(new String[0]));
        }
        final Object locale = entry.get("locale");
        if (locale != null) {
            siteInfo.setLocale((String) locale);
        } else {
            siteInfo.setLocale("en");
        }
        if (entry.get("serverName") != null) {
            siteInfo.setServerName((String) entry.get("serverName"));
        } else {
            siteInfo.setServerName("localhost");
        }
        if (entry.get("title") != null) {
            siteInfo.setTitle((String) entry.get("title"));
        } else {
            siteInfo.setTitle((String) entry.get("siteKey"));
        }
        if (entry.get("description") != null) {
            siteInfo.setDescription((String) entry.get("description"));
        }
        if (entry.get("serverNameAliases") != null) {
            siteInfo.setServerNameAliases((String[]) ((ArrayList) entry.get("serverNameAliases")).toArray(new String[0]));
        }
        if (entry.get("siteAdmin") != null) {
            siteInfo.setSiteAdmin(JahiaUserManagerService.getInstance().lookupUser((String) entry.get("siteAdmin")).getJahiaUser());
        }
        return siteInfo;
    }
}
