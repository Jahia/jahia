/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
        if (!SettingsBean.getInstance().isProcessingServer()) {
            return;
        }

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

        String bundleName = BundleUtils.getDisplayName(bundle);

        try {
            nodeTypeRegistry.addDefinitionsFile(resources, systemId);

            jcrStoreService.deployDefinitions(systemId, module.getVersion().toString(), lastModified);
            logger.info("Registered definitions for bundle {}", bundleName);
        } catch (IOException | ParseException | RepositoryException e) {
            // Error when loading nodetypes. Stop module deployment
            throw new RuntimeException("Error registering node type definition for bundle " + bundle, e);
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
    }
}
