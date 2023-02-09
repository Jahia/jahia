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
package org.jahia.bundles.extender.jahiamodules;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.settings.SettingsBean;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A bundle observer for Jahia Modules. Used by the BundleWatcher defined in the activator.
 */
public class CndBundleObserver implements BundleObserver<URL> {

    private static Logger logger = LoggerFactory.getLogger(CndBundleObserver.class);

    private JCRStoreService jcrStoreService = null;
    private TemplatePackageRegistry templatePackageRegistry;

    public CndBundleObserver() {
        super();
        jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
        templatePackageRegistry = ((JahiaTemplateManagerService) SpringContextSingleton
                .getBean("JahiaTemplateManagerService")).getTemplatePackageRegistry();
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        if (urls.size() == 0) {
            return;
        }

        JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);

        long lastModified = 0;

        List<Resource> resources = new ArrayList<>();
        for (URL url : urls) {
            BundleResource bundleResource = new BundleResource(url, bundle);
            try {
                resources.add(bundleResource);
                long l = bundleResource.lastModified();
                if (l > lastModified) {
                    lastModified = l;
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.debug("Found CND files in bundle " +bundle.getSymbolicName() + " : " + resources);

        ModuleVersion moduleVersion = module.getVersion();
        NodeTypeRegistry nodeTypeRegistry = NodeTypeRegistry.getInstance();
        String systemId = bundle.getSymbolicName();

        try {
            for (Resource resource : resources) {
                module.setDefinitionsFile(resource.getURL().getPath().substring(1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error getting definition file for bundle " + bundle, e);
        }

        boolean latestDefinitions = JCRStoreService.getInstance().isLatestDefinitions(systemId, moduleVersion, lastModified);
        if (!latestDefinitions) {
            return;
        }

        if (!SettingsBean.getInstance().isProcessingServer()) {
            // Need to update deployment properties for browsing nodes
            jcrStoreService.updateDeploymentProperties(systemId, module.getVersion().toString(), lastModified);
            logger.info("Updated deployment properties for bundle {}", systemId);
        } else {
            try {
                nodeTypeRegistry.addDefinitionsFile(resources, systemId);
                jcrStoreService.deployDefinitions(systemId, module.getVersion().toString(), lastModified);
                logger.info("Registered definitions for bundle {}", BundleUtils.getDisplayName(bundle));
            } catch (IOException | ParseException | RepositoryException e) {
                // Error when loading nodetypes. Stop module deployment
                throw new RuntimeException("Error registering node type definition for bundle " + bundle, e);
            }
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
    }
}
