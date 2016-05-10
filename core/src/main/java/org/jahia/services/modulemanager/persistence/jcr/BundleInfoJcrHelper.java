/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.persistence.jcr;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.persistence.PersistedBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for creating tree structure for a bundle in JCR and bundle key to JCR path conversion.
 *
 * @author Ahmed Chaabni
 * @author Sergiy Shyrkov
 */
final class BundleInfoJcrHelper {

    private static final Logger logger = LoggerFactory.getLogger(BundleInfoJcrHelper.class);

    static final String NODE_NAME_BUNDLES = "bundles";

    static final String NODE_NAME_ROOT = "module-management";

    private static final String NODE_TYPE_BUNDLE = "jnt:moduleManagementBundle";

    private static final String NODE_TYPE_FOLDER = "jnt:moduleManagementBundleFolder";

    private static final String NODE_TYPE_ROOT = "jnt:moduleManagement";

    static final String PATH_BUNDLES = '/' + NODE_NAME_ROOT + '/' + NODE_NAME_BUNDLES;

    static final String PATH_ROOT = '/' + NODE_NAME_ROOT;

    /**
     * Looks up the target bundle node for specified bundle key.
     * 
     * @param bundleKey
     *            the bundle key to lookup node for
     * @param session
     *            the current JCR session
     * @return the found JCR node for the bundle or null if the node could not be found
     * @throws RepositoryException
     *             in case of a JCR error
     */
    static JCRNodeWrapper findTargetNode(String bundleKey, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper target = null;
        BundleInfo info = BundleInfo.fromKey(bundleKey);
        String path = getJcrPath(info);
        if (session.nodeExists(path)) {
            target = session.getNode(path);
            logger.debug("Bundle node for key {} found at {}");
        } else if (info.getGroupId() == null) {
            // we have a short key (group ID is omitted), let's search for it
            String query = new StringBuilder(128)
                    .append("select * from [jnt:moduleManagementBundle] where [j:symbolicName]='")
                    .append(JCRContentUtils.sqlEncode(StringUtils.substringBefore(bundleKey, "/")))
                    .append("' and [j:version]='")
                    .append(JCRContentUtils.sqlEncode(StringUtils.substringAfter(bundleKey, "/"))).append("'")
                    .toString();
            logger.debug("Search bundle using query: {}", query);
            JCRNodeIteratorWrapper nodes = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2)
                    .execute().getNodes();
            long resultCount = nodes.getSize();
            if (resultCount > 1) {
                logger.warn("Found multiple ({}) bundle nodes matching key {}. Will take the first one found.",
                        resultCount, bundleKey);
            } else if (resultCount > 0) {
                target = (JCRNodeWrapper) nodes.nextNode();
                if (logger.isDebugEnabled()) {
                    logger.debug("Bundle node for key {} found at {}. Query used: {}",
                            new Object[] { bundleKey, target.getPath(), query });
                }
            } else {
                logger.debug("No bundle for key {} was found. Query used: {}", bundleKey, query);
            }

        }

        return target;
    }

    private static JCRNodeWrapper getBundlesNode(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper bundlesNode = null;
        try {
            bundlesNode = session.getNode(PATH_BUNDLES);
        } catch (PathNotFoundException e) {
            bundlesNode = getRootNode(session).addNode(NODE_NAME_BUNDLES, NODE_TYPE_FOLDER);
        }
        return bundlesNode;
    }

    /**
     * Gets the JCR node path, which corresponds to the specified bundle info.
     * 
     * @param bundleInfo
     *            the bundle info to get JCR path for
     * @return the JCR node path, which corresponds to the specified bundle info
     */
    static String getJcrPath(BundleInfo bundleInfo) {
        StringBuilder path = new StringBuilder(96);
        path.append(PATH_BUNDLES + '/');
        if (StringUtils.isNotEmpty(bundleInfo.getGroupId())) {
            path.append(bundleInfo.getGroupId().replace('.', '/')).append('/');
        }
        path.append(bundleInfo.getSymbolicName()).append('/').append(bundleInfo.getVersion()).append('/')
                .append(bundleInfo.getSymbolicName()).append('-').append(bundleInfo.getVersion()).append(".jar");

        return path.toString();
    }

    /**
     * Get the JCR node path, which corresponds to the specified bundle info.
     * 
     * @param bundleInfo
     *            the bundle info object to get JCR path for
     * @return the JCR node path, which corresponds to the specified bundle info
     */
    static String getJcrPath(PersistedBundle bundleInfo) {
        StringBuilder path = new StringBuilder(96);
        path.append(PATH_BUNDLES + '/');
        if (StringUtils.isNotEmpty(bundleInfo.getGroupId())) {
            path.append(bundleInfo.getGroupId().replace('.', '/')).append('/');
        }
        path.append(bundleInfo.getSymbolicName()).append('/').append(bundleInfo.getVersion()).append('/')
                .append(bundleInfo.getSymbolicName()).append('-').append(bundleInfo.getVersion()).append(".jar");
        return path.toString();
    }

    /**
     * Gets the JCR node path, which corresponds to the specified bundle key.
     * 
     * @param bundleKey
     *            the key of the bundle to get JCR path for
     * @return the JCR node path, which corresponds to the specified bundle key
     */
    static String getJcrPath(String bundleKey) {
        return getJcrPath(BundleInfo.fromKey(bundleKey));
    }

    /**
     * Returns the JCR node, which corresponds to the provided bundle info, creating it if does not exist yet. This method also creates the
     * intermediate JCR structure if not yet created.
     * 
     * @param bundleInfo
     *            the info of the module
     * @param session
     *            the current JCR session
     * @return the JCR node, which corresponds to the provided bundle info
     * @throws RepositoryException
     *             in case of a JCR error
     */
    static JCRNodeWrapper getOrCreateTargetNode(PersistedBundle bundleInfo, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper target = getBundlesNode(session);
        if (bundleInfo.getGroupId() != null) {
            // create sub-folder structure for group ID
            target = mkdirs(target, StringUtils.split(bundleInfo.getGroupId(), '.'), session);
        }
        // create sub-folder for symbolic name
        target = mkdir(target, bundleInfo.getSymbolicName(), session);

        // create sub-folder for the version
        target = mkdir(target, bundleInfo.getVersion(), session);

        String nodeName = bundleInfo.getSymbolicName() + '-' + bundleInfo.getVersion() + ".jar";

        try {
            target = target.getNode(nodeName);
        } catch (PathNotFoundException e) {
            target = target.addNode(nodeName, NODE_TYPE_BUNDLE);
        }

        return target;
    }

    private static JCRNodeWrapper getRootNode(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper root = null;
        try {
            root = session.getNode(PATH_ROOT);
        } catch (PathNotFoundException e) {
            root = session.getRootNode().addNode(NODE_NAME_ROOT, NODE_TYPE_ROOT);
        }

        return root;
    }

    /**
     * Creates the child node for the provided one if not yet exist. Otherwise returns the existing one.
     * 
     * @param startNode
     *            the target node to create child for
     * @param childName
     *            the name of the child node
     * @param session
     *            current JCR session
     * @return the child node
     * @throws RepositoryException
     *             in case of a JCR error
     */
    private static JCRNodeWrapper mkdir(JCRNodeWrapper startNode, String childName, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper child = null;
        try {
            child = startNode.getNode(childName);
        } catch (PathNotFoundException e) {
            child = startNode.addNode(childName, NODE_TYPE_FOLDER);
        }

        return child;
    }

    /**
     * Creates the intermediate JCR tree structure if does not exist and returns the last leaf node.
     * 
     * @param startNode
     *            the start node to create structure from
     * @param pathSegments
     *            an array of recursive node names to create JCR
     * @param session
     *            the current JCR session
     * @return the last leaf node
     * @throws RepositoryException
     *             in case of a JCR error
     */
    private static JCRNodeWrapper mkdirs(JCRNodeWrapper startNode, String[] pathSegments, JCRSessionWrapper session)
            throws RepositoryException {
        for (String childName : pathSegments) {
            startNode = mkdir(startNode, childName, session);
        }
        return startNode;
    }

    /**
     * Initializes an instance of this class.
     */
    private BundleInfoJcrHelper() {
        super();
    }

}
