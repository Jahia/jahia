/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p/>
 * http://www.jahia.com
 * <p/>
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 * <p/>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p/>
 * 1/ GPL
 * ==================================================================================
 * <p/>
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * <p/>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 * <p/>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p/>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.modulemanager.persistence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.modulemanager.impl.BundleServiceImpl;
import org.jahia.services.modulemanager.model.BundleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Responsible for managing the JCR structure and information about deployment of modules.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleInfoPersister implements JahiaAfterInitializationService {

    private TreeMap<String, BundleDTO> bundles;

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        start();
    }


    private static final Logger logger = LoggerFactory.getLogger(ModuleInfoPersister.class);

    private static final String ROOT_NODE_PATH = "/module-management";

    private BundleServiceImpl bundleService;

    /**
     * Checks if the specified bundle is already installed on the current node.
     *
     * @param bundle the bundle
     * @return <code>true</code> if the specified bundle is already installed on the current node; <code>false</code> otherwise
     * @throws RepositoryException in case of a JCR error
     */
    public boolean alreadyInstalled(final BundleDTO bundle) throws RepositoryException {

        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (session.itemExists(ROOT_NODE_PATH + "/bundles/" + bundle.getPath())) {
                    String thisChecksum = session.getNode(ROOT_NODE_PATH + "/bundles/" + bundle.getPath()).getProperty("j:checksum").getString();
                    return StringUtils.equals(thisChecksum, bundle.getChecksum());
                }
                return false;
            }
        });
    }

    /**
     * Injects an instance of the service, which populates the JCR tree structure with module information.
     *
     * @param bundleService an instance of the service, which populates the JCR tree structure with module information
     */
    public void setBundleService(BundleServiceImpl bundleService) {
        this.bundleService = bundleService;
    }

    /**
     * Initialization method that is called by Spring container and aims to validate the JCR tree structure, related to the module
     * management.
     */
    protected void start() {
        try {
            validateJcrTreeStructure();
        } catch (RepositoryException e) {
            logger.error("Unable to validate module management JCR tree structure. Cause: " + e.getMessage(), e);
        }
    }

    public BundleDTO getBundle(String bundleKey) {
        return bundles.get(bundleKey);
    }

    private void validateJcrTreeStructure() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper pathNode = session.getNode("/");
                // 1) ensure module-management skeleton is created in JCR
                if (!pathNode.hasNode("module-management")) {
                    pathNode.addNode("module-management", "jnt:moduleManagement");
                }
                pathNode = pathNode.getNode("module-management");
                if (!pathNode.hasNode("bundles")) {
                    pathNode.addNode("bundles", "jnt:moduleManagementBundleFolder");
                }
                pathNode = pathNode.getNode("bundles");

                bundles = new TreeMap<>();
                Map<String, String> bundeStates = new HashMap<>();

                // 2) populate information about available bundles
                logger.info("Start populating information about available module bundles...");

                bundleService.populateBundles(bundles, bundeStates);
                // Check if '/module-management/bundles' has child nodes or not
                if (!pathNode.hasNodes()) {
                    for (String bundleName : bundles.keySet()) {
                        BundleDTO bundle = bundles.get(bundleName);
                        storeBundle(session, bundle);
                    }
                }

                session.save();
                return null;
            }
        });
    }

    public void addBundle(final BundleDTO bundle) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                bundles.put(bundle.getBundleKey(), bundle);
                storeBundle(session, bundle);
                session.save();
                return null;
            }
        });
    }

    private void storeBundle(JCRSessionWrapper session, BundleDTO bundle) throws RepositoryException {
        JCRNodeWrapper pathNode = session.getNode(ROOT_NODE_PATH + "/bundles");

        final String[] packageFolders = bundle.getPath().split("/");

        for (int i = 0; i < packageFolders.length - 1; i++) {
            if (!pathNode.hasNode(packageFolders[i])) {
                pathNode = pathNode.addNode(packageFolders[i], "jnt:moduleManagementBundleFolder");
            } else {
                pathNode = pathNode.getNode(packageFolders[i]);
            }
        }
        pathNode = pathNode.addNode(packageFolders[packageFolders.length-1],
                "jnt:moduleManagementBundle");
        pathNode.setProperty("j:checksum", bundle.getChecksum());
        pathNode.setProperty("j:displayName", bundle.getDisplayName());
        try {
            InputStream is = new BufferedInputStream(bundle.getJarFile().getInputStream());
            try {
                pathNode.getFileContent().uploadFile(is, "application/jar");
            } finally {
                IOUtils.closeQuietly(is);
            }
        } catch (Exception t) {
            logger.error("file observer error : ", t);
        }
        pathNode.setProperty("j:fileName", bundle.getFileName());
        pathNode.setProperty("j:symbolicName", bundle.getSymbolicName());
        pathNode.setProperty("j:version", bundle.getVersion());
    }

    public String getLocation(BundleDTO bundle) {
        return "jcr:" + bundle.getGroupId() + "/" + bundle.getSymbolicName() + "/" + bundle.getVersion();
    }
}
