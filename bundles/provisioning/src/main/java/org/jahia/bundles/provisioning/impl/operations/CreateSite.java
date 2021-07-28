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

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
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
        }

        return siteInfo;
    }
}
