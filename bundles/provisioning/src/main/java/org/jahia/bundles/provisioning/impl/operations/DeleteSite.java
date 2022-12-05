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

package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.provisioning.Operation;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.JCRTemplate;
import org.jahia.exceptions.JahiaException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Delete site operation
 */

@Component(service = Operation.class, property = "type=deleteSite")
public class DeleteSite implements Operation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteSite.class);
    public static final String DELETE_SITE = "deleteSite";

    @Reference
    private JahiaSitesService sitesService;

    @Reference 
    private JCRTemplate jcrTemplate;

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(DELETE_SITE);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        final String siteKey = (String) entry.get("siteKey");
        
        try {
            jcrTemplate.doExecuteWithSystemSession(session -> {
                try {
                    if (sitesService.siteExists(siteKey, session)) {
                        sitesService.removeSite(sitesService.getSiteByKey(siteKey, session));
                        return null;
                    }
                    LOGGER.warn("No site found with provided siteKey: {}", siteKey);
                } catch(Exception e) {
                    LOGGER.error("Unable to delete the site: {}", siteKey);
                }
                return null;
            });

        } catch (Exception e) {
            LOGGER.error("Unable to delete the site: {}", siteKey);
        }

    }

}
