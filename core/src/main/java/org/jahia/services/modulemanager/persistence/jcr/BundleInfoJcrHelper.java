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

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.PersistedBundle;
import org.osgi.framework.Version;
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

    @SuppressWarnings("unchecked")
    private static Comparator<Version> VERSION_COMPARATOR = ComparatorUtils
            .reversedComparator(new Comparator<Version>() {

                @Override
                public int compare(Version o1, Version o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return -1;
                    }

                    int result = o1.getMajor() - o2.getMajor();
                    if (result != 0) {
                        return result;
                    }

                    result = o1.getMinor() - o2.getMinor();
                    if (result != 0) {
                        return result;
                    }

                    result = o1.getMicro() - o2.getMicro();
                    if (result != 0) {
                        return result;
                    }

                    if (o1.getQualifier().equals(o2.getQualifier())) {
                        return 0;
                    }

                    if ("SNAPSHOT".equals(o1.getQualifier())) {
                        return -1;
                    }

                    if ("SNAPSHOT".equals(o2.getQualifier())) {
                        return 1;
                    }

                    return o1.getQualifier().compareTo(o2.getQualifier());
                }
            });

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
        String groupId = null;
        String symbolicName = null;
        String version = null;
        BundleInfo info = null;
        if (bundleKey.indexOf('/') == -1) {
            // we have only symbolic name
            symbolicName = bundleKey;
        } else {
            info = BundleInfo.fromKey(bundleKey);
            groupId = info.getGroupId();
            symbolicName = info.getSymbolicName();
            version = info.getVersion();
        }
        if (info != null) {
            String path = getJcrPath(info);
            if (session.nodeExists(path)) {
                target = session.getNode(path);
                logger.debug("Bundle node for key {} found at {}");
            }
        }

        if (target == null && (groupId == null || version == null)) {
            // we do not have a full key (group ID or/and version are omitted), let's search for it
            target = guessTargetNode(bundleKey, groupId, symbolicName, version, session);
        }

        return target;
    }

    /**
     * Tries to find the target bundle node based on the info we have.
     * <p>
     * We use a JCR query for bundle with symbolic name, group ID / version (if available). If query delivers multiple results we try our
     * best to find the best matching node: in case no version was specified, we find the result with the highest version; if the version
     * was specified, we log a warning that multiple candidates match the requested bundle (meaning we have multiple bundles with same
     * symbolic name and version, but different group ID) and we take the first one from the list of matching nodes.
     * 
     * @param bundleKey
     *            the original bundle key
     * @param groupId
     *            the group ID for the bundle
     * @param symbolicName
     *            bundle symbolic name
     * @param version
     *            the version of the bundle
     * @param session
     *            current JCR session
     * @return the found bundle or <code>null</code> if no bundle could be found
     * @throws RepositoryException
     *             in case of JCR errors
     */
    private static JCRNodeWrapper guessTargetNode(String bundleKey, String groupId, String symbolicName, String version,
            JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper target = null;
        StringBuilder queryBuilder = new StringBuilder(128)
                .append("select * from [jnt:moduleManagementBundle] where [j:symbolicName]='")
                .append(JCRContentUtils.sqlEncode(symbolicName)).append("'");

        if (version != null) {
            queryBuilder.append(" and [j:version]='").append(JCRContentUtils.sqlEncode(version)).append("'");
        }
        if (groupId != null) {
            queryBuilder.append(" and [j:groupId]='").append(JCRContentUtils.sqlEncode(groupId)).append("'");
        }

        String query = queryBuilder.toString();
        logger.debug("Search bundle using query: {}", query);
        JCRNodeIteratorWrapper nodes = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2)
                .execute().getNodes();
        long resultCount = nodes.getSize();
        if (resultCount > 1) {
            if (version != null) {
                throw new ModuleManagementException("Found multiple (" + resultCount + ") bundle nodes matching key "
                        + bundleKey + ". Use unique bundle key (with group ID).");
            } else {
                Map<Version, JCRNodeWrapper> matchingNodes = new TreeMap<>(VERSION_COMPARATOR);
                String foundGroupId = null;
                while (nodes.hasNext()) {
                    JCRNodeWrapper candidate = (JCRNodeWrapper) nodes.nextNode();
                    if (groupId == null) {
                        // need to check the group
                        if (foundGroupId != null) {
                            if (!StringUtils.equals(foundGroupId, candidate.getPropertyAsString("j:groupId"))) {
                                // we have in the result bundles from different groups -> non-unique result
                                throw new ModuleManagementException(
                                        "Found multiple (" + resultCount + ") bundle nodes matching key " + bundleKey
                                                + ". Use unique bundle key (with group ID).");
                            }
                        } else {
                            // first result: read groupID
                            foundGroupId = candidate.getPropertyAsString("j:groupId");
                        }
                    }
                    matchingNodes.put(candidate.hasProperty("j:version")
                            ? new Version(candidate.getPropertyAsString("j:version")) : null, candidate);
                }
                Entry<Version, JCRNodeWrapper> match = matchingNodes.entrySet().iterator().next();
                target = match.getValue();
                logger.info(
                        "Found multiple ({}) bundle nodes matching key {}. Will take the one with highest version: {}.",
                        new Object[] { resultCount, bundleKey, match.getKey() });
            }
        } else if (resultCount > 0) {
            target = (JCRNodeWrapper) nodes.nextNode();
            if (logger.isDebugEnabled()) {
                logger.debug("Bundle node for key {} found at {}. Query used: {}",
                        new Object[] { bundleKey, target.getPath(), query });
            }
        } else {
            logger.debug("No bundle for key {} was found. Query used: {}", bundleKey, query);
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

    /**
     * Returns the module manager JCR root node, creating it if does not exist yet.
     * 
     * @param session
     *            the current JCR session
     * @return the module manager JCR root node, creating it if does not exist yet
     * @throws RepositoryException
     *             in case of JCR errors
     */
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
