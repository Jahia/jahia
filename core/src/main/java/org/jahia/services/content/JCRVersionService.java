/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.iterator.VersionIteratorAdapter;
import org.jahia.utils.comparator.NumericStringComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRFrozenNodeAsRegular;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import java.util.*;

/**
 * Version listing and retrieval service. This service offers tools to list the versions in a user-friendly way,
 * collapsing internal versions into date-related changes, retrieving comments, etc...
 *
 * @author loom
 *         Date: Mar 10, 2010
 *         Time: 10:06:02 AM
 */
public class JCRVersionService extends JahiaService {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss");

    private static JCRVersionService instance;

    private Set<String> versionedTypes;

    private Set<String> excludedVersionedTypes;

    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(JCRVersionService.class);

    /**
     * Get the singleton instance of the JCRPublicationService
     *
     * @return the singleton instance of the JCRPublicationService
     */
    public synchronized static JCRVersionService getInstance() {
        if (instance == null) {
            instance = new JCRVersionService();
        }
        return instance;
    }

    private Set<String> mixinsToRemoveOnDestination = new HashSet<String>(
            Arrays.asList(new String[] { "jmix:referencesInField" }));

    private Set<String> nodetypesToSkipWhenSynchronizing = new HashSet<String>(
            Arrays.asList(new String[] { "jnt:referenceInField" }));

    @Override
    public void start() throws JahiaInitializationException {
    }

    @Override
    public void stop() throws JahiaException {
    }

    public void setVersionedTypes(String versionedTypes) {
        this.versionedTypes = JCRContentUtils.splitAndUnify(versionedTypes, " ,");
    }

    public void setExcludedVersionedTypes(String excludedVersionedTypes) {
        this.excludedVersionedTypes = JCRContentUtils.splitAndUnify(excludedVersionedTypes, " ,");
    }

    public Set<String> getVersionedTypes() {
        return versionedTypes;
    }

    public Set<String> getExcludedVersionedTypes() {
        return excludedVersionedTypes;
    }

    /**
     * Retrieves the list of versions, ignoring internal version created in the publication process.
     *
     * @param session the session to use to retrieve the versions
     * @param node    the node for which to retrieve the versions
     * @return a List of VersionInfo objects containing the resolved versions, as well as extra information such as the
     *         checkinDate if available.
     * @throws RepositoryException happens if there was a problem retrieving the list of versions.
     */
    public List<VersionInfo> getVersionInfos(Session session, JCRNodeWrapper node) throws RepositoryException {
        VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory(node.getPath());

        VersionIterator versions = versionHistory.getAllVersions();
        return getVersionsInfos(versionHistory, versions);
    }

    public List<VersionInfo> getLinearVersionInfos(Session session, JCRNodeWrapper node) throws RepositoryException {
        VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory(node.getPath());

        VersionIterator versions = versionHistory.getAllLinearVersions();
        return getVersionsInfos(versionHistory, versions);
    }

    private List<VersionInfo> getVersionsInfos(VersionHistory versionHistory, VersionIterator versions) throws RepositoryException {
        if (versions.hasNext()) {
            versions.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        List<VersionInfo> versionList = new ArrayList<VersionInfo>();
        while (versions.hasNext()) {
            Version v = versions.nextVersion();
            String[] versionLabels = versionHistory.getVersionLabels(v);
            if (versionLabels != null && versionLabels.length > 0) {
                for (String string : versionLabels) {
                    VersionInfo versionInfo = new VersionInfo(v, string, 0);
                    versionList.add(versionInfo);
                }
            }
        }
        Collections.sort(versionList,new NumericStringComparator<VersionInfo>(((JCRSessionWrapper) versionHistory.getSession()).getLocale()));
        return versionList;
    }


    /**
     * Finds the closest version in a version history to a specific date.
     *
     * @param vh          the version history in which to lookup versions
     * @param versionDate the date to compare with. Note that it will find the closest version at OR BEFORE the date
     * @return the closest version at or before the date specified.
     * @throws RepositoryException in case of JCR-related errors
     */
    public static Version findClosestVersion(VersionHistory vh, Date versionDate) throws RepositoryException {
        VersionIterator vi = null;
        try {
            vi = vh.getAllLinearVersions();
        } catch (ItemNotFoundException e) {
            String[] labels = vh.getVersionLabels();
            for (String label : labels) {
                if (label.startsWith(vh.getSession().getWorkspace().getName()+"_removed")) {
                    try {
                        Date removedAt = DATE_FORMAT.parseDateTime(StringUtils.substringAfter(label, vh.getSession().getWorkspace().getName() + "_removed_at_")).toDate();
                        if (removedAt.before(versionDate)) {
                            return null;
                        }
                    } catch (IllegalArgumentException e1) {
                        logger.error("Cannot parse deletion date for label "+label ,e1);
                        return null;
                    }
                    Version base = vh.getVersionByLabel(label);
                    LinkedList<Version> versions = new LinkedList<Version>();
                    while (base != null) {
                        versions.addFirst(base);
                        Version[] preds = base.getPredecessors();
                        if (preds.length == 0) {
                            base = null;
                        } else {
                            base = preds[0];
                        }
                    }
                    vi = new VersionIteratorAdapter(versions);
                    break;
                }
            }
            if (vi == null) {
                return null;
            }
        }

        if (vi.getSize() <= 1) {
            return null;
        }

        Version lastVersion = null;
        Version closestVersion = null;
        if (vi.hasNext()) {
            vi.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        String nodeTitle = null;
        StringBuilder propertyString = null;
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            if (logger.isDebugEnabled()) {
                try {
                    Node frozenNode = v.getFrozenNode();
                    propertyString = new StringBuilder();
                    PropertyIterator propertyIterator = frozenNode.getProperties();
                    while (propertyIterator.hasNext()) {
                        Property property = propertyIterator.nextProperty();
                        propertyString.append("  ");
                        propertyString.append(property.getName());
                        propertyString.append("=");
                        if (property.isMultiple()) {
                            for (Value value : property.getValues()) {
                                propertyString.append(value.getString());
                                propertyString.append(",");
                            }
                        } else {
                            propertyString.append(property.getValue().getString());
                        }
                        propertyString.append("\n");
                    }
                } catch (IllegalStateException e) {
                    propertyString.append(e.getMessage()).append("\n");
                }
            }
            Date checkinDate = null;
            boolean checkinDateAvailable = false;
            if (v.getCreated().getTime().compareTo(versionDate) > 0) {
                // this can happen if we have a checkinDate, but try to resolve using the creation date.
                closestVersion = lastVersion;
                break;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Version " + v.getName() + " checkinDateAvailable=" + checkinDateAvailable + " checkinDate=" +
                                checkinDate + " created=" + v.getCreated().getTime() + " properties:" +
                                propertyString.toString());
            }
            lastVersion = v;
        }
        if (closestVersion == null && lastVersion != null) {
            // if we haven't found anything, maybe it's the last version that we should be using ?
            if (lastVersion.getCreated().getTime().compareTo(versionDate) <= 0) {
                closestVersion = lastVersion;
            }
        }
        if (closestVersion!=null && logger.isDebugEnabled()) {
            logger.debug("Resolved date " + versionDate + " for node title " + nodeTitle + " to closest version " +
                    closestVersion.getName() + " createdTime=" + closestVersion.getCreated().getTime());
        }
        return closestVersion;
    }

    public void restoreVersionLabel(final JCRNodeWrapper node, final Date versionDate, final String label, final boolean allSubTree) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, node.getSession().getWorkspace().getName(), null,
                new JCRCallback<Object>() {
                    public Object doInJCR(final JCRSessionWrapper session) throws RepositoryException {
                        String workspace = label != null ? StringUtils.substringBefore(label, "_") : "live";
                        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null,
                                new JCRCallback<Object>() {
                                    public Object doInJCR(final JCRSessionWrapper frozensession) throws RepositoryException {
                                        JCRNodeWrapper destinationNode = session.getNodeByUUID(node.getIdentifier());
                                        VersionManager versionManager = session.getWorkspace().getVersionManager();
                                        String path = destinationNode.getPath();
                                        if (!versionManager.isCheckedOut(path)) {
                                            versionManager.checkout(path);
                                        }

                                        // Todo: first get frozen node for this label
                                        frozensession.setVersionLabel(label);
                                        frozensession.setVersionDate(versionDate);
                                        JCRNodeWrapper frozenVersionAsRegular = frozensession.getNodeByUUID(destinationNode.getIdentifier());

                                        if (frozenVersionAsRegular == null) {
                                            throw new RepositoryException("label version " + label + " could not be found on node " + destinationNode.getPath());
                                        }
                                        synchronizeNode(frozenVersionAsRegular, destinationNode, session, allSubTree);
                                        session.save();
                                        return null;
                                    }
                                });
                        return null;
                    }
                });
    }

    private void synchronizeNode(final JCRNodeWrapper frozenNode, final JCRNodeWrapper destinationNode,
                                 JCRSessionWrapper session, boolean allSubTree)
            throws RepositoryException {
        session.checkout(destinationNode);

        NodeType[] mixin = destinationNode.getMixinNodeTypes();
        for (NodeType aMixin : mixin) {
            String mixinName = aMixin.getName();
            if (mixinsToRemoveOnDestination.contains(mixinName)) {
                logger.info("Removing mixin " + mixinName + " on node " + destinationNode.getPath());
                destinationNode.removeMixin(mixinName);
            }
        }

        mixin = frozenNode.getMixinNodeTypes();
        for (NodeType aMixin : mixin) {
            if(!Constants.forbiddenMixinToCopy.contains(aMixin.getName())) {
                logger.info("Adding mixin "+aMixin.getName()+" on node "+destinationNode.getPath());
                destinationNode.addMixin(aMixin.getName());
            }
        }

        if (frozenNode.hasProperty("jcr:language")) {
            destinationNode.setProperty("jcr:language", frozenNode.getProperty("jcr:language").getString());
        }

        PropertyIterator props = frozenNode.getProperties();

        List<String> names = new ArrayList<String>();
        while (props.hasNext()) {
            Property property = props.nextProperty();
            String propertyName = property.getName();
            names.add(propertyName);
            logger.info("Checking property for updating "+propertyName+" from source node "+destinationNode.getPath());
            try {
                if (!property.getDefinition().isProtected() &&
                        !Constants.forbiddenPropertiesToCopy.contains(propertyName)) {
                    logger.info("Setting property "+propertyName+" on node "+destinationNode.getPath());
                    if (property.getDefinition().isMultiple() && property.isMultiple()) {
                        destinationNode.setProperty(propertyName, property.getValues());
                    } else {
                        destinationNode.setProperty(propertyName, property.getValue());
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to copy property '" + propertyName + "'. Skipping.", e);
            }
        }

        PropertyIterator pi = destinationNode.getProperties();
        while (pi.hasNext()) {
            JCRPropertyWrapper oldChild = (JCRPropertyWrapper) pi.next();
            logger.info("Checking property for removal "+oldChild.getName()+" from destination node "+destinationNode.getPath());
            if (!oldChild.getDefinition().isProtected()) {
                if (!names.contains(oldChild.getName())) {
                    logger.info("Removing property "+oldChild.getName()+" on node "+destinationNode.getPath());
                    oldChild.remove();
                }
            }
        }

        mixin = destinationNode.getMixinNodeTypes();
        for (NodeType aMixin : mixin) {
            if (!frozenNode.isNodeType(aMixin.getName()) &&
                    !Constants.forbiddenMixinToCopy.contains(aMixin.getName())) {
                logger.info("Removing mixin "+aMixin.getName()+" on node "+destinationNode.getPath());
                destinationNode.removeMixin(aMixin.getName());
            }
        }

        Map<String, JCRNodeWrapper> destinationNodes = new HashMap<String, JCRNodeWrapper>();
        NodeIterator ni = destinationNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            destinationNodes.put(n.getIdentifier(), n);
        }

        names.clear();
        Set<String> skippedNodeNames = new HashSet<>();
        ni = frozenNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            // do not handle rights on the node when restore a node
            if (!child.getName().equals("j:acl")) {
                names.add(child.getName());
                if (destinationNodes.containsKey(child.getIdentifier())) {
                    JCRNodeWrapper node = destinationNodes.remove(child.getIdentifier());
                    synchronizeNode(child, node, session, allSubTree);
                } else if (child.getRealNode().getParent().isNodeType(Constants.NT_FROZENNODE)) {
                    String primaryNodeType = child.getPrimaryNodeType().getName();
                    if (nodetypesToSkipWhenSynchronizing.contains(primaryNodeType)) {
                        // keep track of the child nodes to skip, so they can also be ignored when ordering them
                        skippedNodeNames.add(child.getName());
                    } else {
                        JCRNodeWrapper node = destinationNode.addNode(child.getName(), primaryNodeType);
                        synchronizeNode(child, node, session, allSubTree);
                    }
                } else {
                    VersionHistory history;
                    try {
                        history = (VersionHistory) child.getRealNode().getProperty(
                                Constants.JCR_VERSIONHISTORY).getNode();
                    } catch (RepositoryException e){
                        history = (VersionHistory) child.getRealNode().getParent().getParent();
                    }
                    Version version = findVersionByLabel(history, ((JCRFrozenNodeAsRegular) child).getVersionLabel());
                    if (version == null) {
                        version = findClosestVersion(history, ((JCRFrozenNodeAsRegular) child).getVersionDate());
                    }
                    if (version != null) {
                        session.save();
                        logger.info("Restoring node "+child.getPath()+" on parent "+destinationNode.getPath());
                        session.getWorkspace().getVersionManager().restore(child.getPath(),version, false);
                        JCRNodeWrapper node = session.getNode(child.getPath(), false);
                        synchronizeNode(child, node, session, allSubTree);
                    }
                    //child.copy(destinationNode, child.getName(), false);
                }
            }
        }

        for (JCRNodeWrapper oldChild : destinationNodes.values()) {
            if (!names.contains(oldChild.getName())) {
                if ((!oldChild.isNodeType("jmix:publication") || allSubTree) && !oldChild.isNodeType("jnt:translation")) {
                    logger.info("Removing node "+oldChild.getName()+" on node "+destinationNode.getPath());
                    oldChild.remove();
                }
            }
        }

        if (destinationNode.getPrimaryNodeType().hasOrderableChildNodes()) {
            Collections.reverse(names);
            String previous = null;
            for (String name : names) {
                if (skippedNodeNames.contains(name)) {
                    logger.debug("Skipping node {} on node {}", name, destinationNode.getPath());
                    continue;
                }
                destinationNode.orderBefore(name, previous);
                previous = name;
            }
        }
        if (destinationNode.isNodeType(Constants.MIX_LAST_MODIFIED)) {
            destinationNode.setProperty(Constants.JCR_LASTMODIFIED,GregorianCalendar.getInstance());
        }
    }

    public static Version findVersionByLabel(VersionHistory vh, String label) throws RepositoryException {
        if (label != null && !"".equals(label.trim())) {
            if (vh.hasVersionLabel(label.trim())) {
                VersionIterator allVersions = vh.getAllVersions();
                while (allVersions.hasNext()) {
                    Version version = allVersions.nextVersion();
                    if (Arrays.asList(vh.getVersionLabels(version)).contains(label.trim())) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    public void addVersionLabel(final JCRNodeWrapper node, final String label) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, node.getSession().getWorkspace().getName(), null,
                session -> {
                    JCRNodeWrapper nodeWrapper = session.getNodeByUUID(node.getIdentifier());
                    if (nodeWrapper.isVersioned()) {
                        VersionManager versionManager = session.getWorkspace().getVersionManager();
                        VersionHistory versionHistory = versionManager.getVersionHistory(node.getPath());
                        String labelWithWs = node.getSession().getWorkspace().getName() + "_" + label;
                        if (!versionHistory.hasVersionLabel(labelWithWs)) {
                            Version version = versionManager.getBaseVersion(node.getPath());
                            if (logger.isDebugEnabled()) {
                                logger.debug("Add version label " + labelWithWs + " on " + node.getPath() + " for version " +
                                        version.getName());
                            }
                            versionHistory.addVersionLabel(version.getName(), labelWithWs, true);
                        }
                    }
                    return null;
                });
    }

    public void addVersionLabel(final List<String> allUuids, final String label, final String workspace)
            throws RepositoryException {
        if (allUuids == null || allUuids.isEmpty()) {
            return;
        }
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, session -> {
            VersionManager versionManager = session.getWorkspace().getVersionManager();
            final int batchSize = 100;
            for (int i = 0; i < allUuids.size(); i++) {
                String allUuid = allUuids.get(i);
                try {
                    JCRNodeWrapper nodeWrapper = session.getNodeByUUID(allUuid);
                    if (nodeWrapper.isVersioned() && JCRContentUtils.needVersion(nodeWrapper, versionedTypes, excludedVersionedTypes)) {
                        String path = nodeWrapper.getPath();
                        VersionHistory versionHistory = versionManager.getVersionHistory(path);
                        String labelWithWs = workspace + "_" + label;
                        if (!versionHistory.hasVersionLabel(labelWithWs)) {
                            Version version = versionManager.getBaseVersion(path);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Add version label " + labelWithWs + " on " + path + " for version " +
                                        version.getName());
                            }
                            versionHistory.addVersionLabel(version.getName(), labelWithWs, true);
                        }
                    }
                } catch (RepositoryException e) {
                    logger.debug(e.getMessage(), e);
                } finally {
                    // Flush session internal cache every 100 items, to avoid memory issues
                    // The current function can be called with a large number of UUIDs in during import.
                    // We do not really benefit from the internal cache in current case, I would disable it if there was a way to do it easily
                    // But for now, we just flush it every 100 items
                    if ((i + 1) % batchSize == 0) {
                        session.refresh(false);
                    }
                }
            }
            return null;
        });
    }

    public void setMixinsToRemoveOnDestination(Set<String> mixinsToRemoveOnDestination) {
        this.mixinsToRemoveOnDestination = mixinsToRemoveOnDestination;
    }
}
