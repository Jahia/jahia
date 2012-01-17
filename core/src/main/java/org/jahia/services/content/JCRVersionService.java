/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
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

    private static JCRVersionService instance;

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


    @Override
    public void start() throws JahiaInitializationException {
    }

    @Override
    public void stop() throws JahiaException {
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
        if (versions.hasNext()) {
            versions.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        Set<VersionInfo> versionList = new TreeSet<VersionInfo>();
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
        return new ArrayList<VersionInfo>(versionList);
    }


    /**
     * Finds the closest version in a version history to a specific date.
     *
     * @param vh          the version history in which to lookup versions
     * @param versionDate the date to compare with. Note that it will find the closest version at OR BEFORE the date
     * @return the closest version at or before the date specified.
     * @throws RepositoryException
     */
    public static Version findClosestVersion(VersionHistory vh, Date versionDate) throws RepositoryException {
        VersionIterator vi = null;
        try {
            vi = vh.getAllLinearVersions();
        } catch (ItemNotFoundException e) {
            String[] labels = vh.getVersionLabels();
            for (String label : labels) {
                if (label.startsWith(vh.getSession().getWorkspace().getName()+"_removed")) {
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
                    vi = new VersionIteratorImpl(versions.iterator(), versions.size());
                    break;
                }
            }
            if (vi == null) {
                return null;
            }
        }


        Version lastVersion = null;
        Version closestVersion = null;
        if (vi.hasNext()) {
            vi.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        String nodeTitle = null;
        StringBuffer propertyString = null;
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            if (logger.isDebugEnabled()) {
                try {
                    Node frozenNode = v.getFrozenNode();
                    propertyString = new StringBuffer();
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
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, node.getSession().getWorkspace().getName(), null,
                new JCRCallback<Object>() {
                    public Object doInJCR(final JCRSessionWrapper session) throws RepositoryException {
                        String workspace = label!=null?StringUtils.substringBefore(label, "_"):"live";
                        JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, null,
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

                                        if(frozenVersionAsRegular==null) {
                                            throw new RepositoryException("label version " + label + " could not be found on node "+destinationNode.getPath());
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

        NodeType[] mixin = frozenNode.getMixinNodeTypes();
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
                    JCRNodeWrapper node = destinationNode.addNode(child.getName(), child.getPrimaryNodeType().getName());
                    synchronizeNode(child, node, session, allSubTree);
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
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, node.getSession().getWorkspace().getName(), null,
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper nodeWrapper = session.getNodeByUUID(node.getIdentifier());
                        VersionManager versionManager = session.getWorkspace().getVersionManager();
                        VersionHistory versionHistory = versionManager.getVersionHistory(node.getPath());
                        String labelWithWs = node.getSession().getWorkspace().getName() + "_" + label;
                        if (!versionHistory.hasVersionLabel(labelWithWs)) {
                            Version version = versionManager.getBaseVersion(node.getPath());
                            logger.debug("Add version label " + labelWithWs + " on " + node.getPath() + " for version " +
                                    version.getName());
                            if (nodeWrapper.isVersioned()) {
                                versionHistory.addVersionLabel(version.getName(), labelWithWs, true);
                            }
                        }
                        return null;
                    }
                });

    }

    public void addVersionLabel(final List<String> allUuids, final String label, final String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, null, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                VersionManager versionManager = session.getWorkspace().getVersionManager();
                for (String allUuid : allUuids) {
                    try {
                        JCRNodeWrapper nodeWrapper = session.getNodeByUUID(allUuid);
                        VersionHistory versionHistory = versionManager.getVersionHistory(nodeWrapper.getPath());
                        String labelWithWs = workspace + "_" + label;
                        if (!versionHistory.hasVersionLabel(labelWithWs)) {
                            Version version = versionManager.getBaseVersion(nodeWrapper.getPath());
                            logger.debug("Add version label " + labelWithWs + " on " + nodeWrapper.getPath() + " for version " +
                                    version.getName());
                            if (nodeWrapper.isVersioned()) {
                                versionHistory.addVersionLabel(version.getName(), labelWithWs, true);
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.debug(e.getMessage(), e);
                    }
                }
                return null;
            }
        });
    }
}
