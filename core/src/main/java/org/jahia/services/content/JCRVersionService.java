/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.apache.log4j.Logger;
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

    private static transient Logger logger = Logger.getLogger(JCRVersionService.class);

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
        boolean live = session.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE);
        VersionIterator versions = versionHistory.getAllVersions();
        if (versions.hasNext()) {
            Version v = versions.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        Set<VersionInfo> versionList = new TreeSet<VersionInfo>();
        while (versions.hasNext()) {
            Version v = versions.nextVersion();
            Calendar checkinDate = v.getCreated();
            String[] versionLabels = versionHistory.getVersionLabels(v);
            if (versionLabels != null && versionLabels.length > 0) {
                for (String string : versionLabels) {
                    if(!string.contains("published") || string.contains(session.getWorkspace().getName())) {
                        VersionInfo versionInfo = new VersionInfo(v, checkinDate, string, 0);
                        versionList.add(versionInfo);
                    }
                }
            }
        }
        return new ArrayList<VersionInfo>(versionList);
    }

    public Calendar setNodeCheckinDate(Node node, Calendar checkinDate) throws RepositoryException {
        if (!node.isNodeType("jmix:versionInfo")) {
            return null;
        }
//        if (node.hasProperty("j:checkinDate")) {
//            Calendar currentDate = node.getProperty("j:checkinDate").getDate();
//        }

//        node.setProperty("j:checkinDate", checkinDate);
        return checkinDate;
    }


    public void checkin(Session session, JCRNodeWrapper node, Calendar checkinDate) throws RepositoryException {
        setNodeCheckinDate(node, checkinDate);
        session.getWorkspace().getVersionManager().checkin(node.getPath());
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
        VersionIterator vi = vh.getAllLinearVersions();
        Version lastVersion = null;
        Version closestVersion = null;
        if (vi.hasNext()) {
            Version v = vi.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        String nodeTitle = null;
        StringBuffer propertyString = null;
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            Node frozenNode = v.getFrozenNode();
            if (logger.isDebugEnabled()) {
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

    public void addVersionLabel(final JCRNodeWrapper node, final String label) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, node.getSession().getWorkspace().getName(), null,
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper nodeWrapper = session.getNodeByUUID(node.getIdentifier());
                        VersionManager versionManager = session.getWorkspace().getVersionManager();
                        VersionHistory versionHistory = versionManager.getVersionHistory(node.getPath());
                        if (versionManager.isCheckedOut(nodeWrapper.getPath())) {
                            logger.debug("Checkin node " + nodeWrapper.getPath());
                            versionManager.checkin(nodeWrapper.getPath());
                        }
                        if (!versionHistory.hasVersionLabel(label)) {
                            Version version = versionManager.getBaseVersion(node.getPath());
                            logger.debug("Add version label " + label + " on " + node.getPath() + " for version " +
                                         version.getName());
                            if (nodeWrapper.isVersioned()) {
                                versionHistory.addVersionLabel(version.getName(), label, true);
                            }
                            if (nodeWrapper.hasNodes()) {
                                NodeIterator iterator = nodeWrapper.getNodes();
                                while (iterator.hasNext()) {
                                    JCRNodeWrapper nodeWrapper1 = (JCRNodeWrapper) iterator.nextNode();
                                    if (nodeWrapper1.isVersioned()) {
                                        addVersionLabel(nodeWrapper1, label);
                                    }
                                }
                            }
                        }
                        return null;
                    }
                });

    }

    public void restoreVersionLabel(final JCRNodeWrapper node, final String label) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, node.getSession().getWorkspace().getName(), null,
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper nodeWrapper = session.getNodeByUUID(node.getIdentifier());
                        VersionManager versionManager = session.getWorkspace().getVersionManager();
                        String path = nodeWrapper.getPath();
                        if (!versionManager.isCheckedOut(path)) {
                            versionManager.checkout(path);
                        }
                        // Todo: first get frozen node for thislabel
                        session.setVersionLabel(label);

                        JCRNodeWrapper frozenVersionAsRegular = session.getNodeByUUID(node.getIdentifier());
                        if(frozenVersionAsRegular==null) {
                            throw new RepositoryException("label version " + label + " could not be found on node "+nodeWrapper.getPath());
                        }
                        synchronizeNode(frozenVersionAsRegular, nodeWrapper, session, true);
                        session.save();
                        return null;
                    }
                });
    }

    private void synchronizeNode(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
                                 JCRSessionWrapper session, boolean doRemove)
            throws RepositoryException {
        session.checkout(destinationNode);

        NodeType[] mixin = source.getMixinNodeTypes();
        for (NodeType aMixin : mixin) {
            if(!Constants.forbiddenMixinToCopy.contains(aMixin.getName())) {
                logger.info("Adding mixin "+aMixin.getName()+" on node "+destinationNode.getPath());
                destinationNode.addMixin(aMixin.getName());
            }
        }

        if (source.hasProperty("jcr:language")) {
            destinationNode.setProperty("jcr:language", source.getProperty("jcr:language").getString());
        }

        PropertyIterator props = source.getProperties();

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
            if (!source.isNodeType(aMixin.getName()) &&
                !Constants.forbiddenMixinToCopy.contains(aMixin.getName())) {
                logger.info("Removing mixin "+aMixin.getName()+" on node "+destinationNode.getPath());
                destinationNode.removeMixin(aMixin.getName());
            }
        }

        NodeIterator ni = source.getNodes();
        names.clear();
        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            names.add(child.getName());

            if (destinationNode.hasNode(child.getName())) {
                JCRNodeWrapper node = destinationNode.getNode(child.getName());
                synchronizeNode(child, node, session, doRemove);
            } else {
                VersionHistory history;
                try {
                    history = (VersionHistory) child.getRealNode().getProperty(
                            Constants.JCR_VERSIONHISTORY).getNode();
                } catch (RepositoryException e){
                    history = (VersionHistory) child.getRealNode().getParent().getParent();
                }
                Version byLabel = findVersionByLabel(history, ((JCRFrozenNodeAsRegular) child).getVersionLabel());
                session.save();
                logger.info("Restoring node "+child.getPath()+" on parent "+destinationNode.getPath());
                session.getWorkspace().getVersionManager().restore(child.getPath(),byLabel, false);
                JCRNodeWrapper node = session.getNode(child.getPath(), false);
                restoreVersionLabel(node, ((JCRFrozenNodeAsRegular) child).getVersionLabel());
                //child.copy(destinationNode, child.getName(), false);
            }
        }
        if (doRemove) {
            ni = destinationNode.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper oldChild = (JCRNodeWrapper) ni.next();
                if (!names.contains(oldChild.getName())) {
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
        destinationNode.setProperty(Constants.JCR_LASTMODIFIED,GregorianCalendar.getInstance());
    }

    public static Version findVersionByLabel(VersionHistory vh, String label) throws RepositoryException {
        if (vh.hasVersionLabel(label)) {
            VersionIterator allVersions = vh.getAllVersions();
            while (allVersions.hasNext()) {
                Version version = allVersions.nextVersion();
                if (Arrays.asList(vh.getVersionLabels(version)).contains(label)) {
                    return version;
                }
            }
        }
        return null;
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
                        if (versionManager.isCheckedOut(nodeWrapper.getPath())) {
                            versionManager.checkin(nodeWrapper.getPath());
                        }
                        if (!versionHistory.hasVersionLabel(label)) {
                            Version version = versionManager.getBaseVersion(nodeWrapper.getPath());
                            logger.debug("Add version label " + label + " on " + nodeWrapper.getPath() + " for version " +
                                         version.getName());
                            if (nodeWrapper.isVersioned()) {
                                versionHistory.addVersionLabel(version.getName(), label, true);
                            }
//                            if (nodeWrapper.hasNodes()) {
//                                NodeIterator iterator = nodeWrapper.getNodes();
//                                while (iterator.hasNext()) {
//                                    JCRNodeWrapper nodeWrapper1 = (JCRNodeWrapper) iterator.nextNode();
//                                    if (nodeWrapper1.isVersioned()) {
//                                        addVersionLabel(nodeWrapper1, label);
//                                    }
//                                }
//                            }
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
