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
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;

import javax.jcr.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
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
     * @param session the session to use to retrieve the versions
     * @param node the node for which to retrieve the versions
     * @return a List of VersionInfo objects containing the resolved versions, as well as extra information such as the
     * checkinDate if available.
     * @throws RepositoryException happens if there was a problem retrieving the list of versions.
     */
    public List<VersionInfo> getVersionInfos(Session session, JCRNodeWrapper node) throws RepositoryException {
        VersionHistory versionHistory = session.getWorkspace().getVersionManager().getVersionHistory(node.getPath());
        VersionIterator versions = versionHistory.getAllLinearVersions();
        if (versions.hasNext()) {
            Version v = versions.nextVersion();
            // the first is the root version, which has no properties, so we will ignore it.
        }
        Set<VersionInfo> versionList = new TreeSet<VersionInfo>();
        while (versions.hasNext()) {
            Version v = versions.nextVersion();
            JCRNodeWrapper versionNode = node.getFrozenVersion(v.getName());
            Calendar checkinDate = null;
            if (versionNode.hasProperty("j:checkinDate")) {
                checkinDate = versionNode.getProperty("j:checkinDate").getDate();
            }
            VersionInfo versionInfo = new VersionInfo(v, checkinDate, "", 0);
            if (versionList.contains(versionInfo)) {
                versionList.remove(versionInfo); // we remove to keep only the latest one.
            }
            versionList.add(versionInfo);
        }
        return new ArrayList<VersionInfo>(versionList);
    }

    public Calendar setNodeCheckinDate(Node node, Calendar checkinDate) throws RepositoryException {
        if (!node.isNodeType("jmix:versionInfo")) {
            return null;
        }
        if (node.hasProperty("j:checkinDate")) {
            Calendar currentDate = node.getProperty("j:checkinDate").getDate();
        }

        node.setProperty("j:checkinDate", checkinDate);
        return checkinDate;
    }


    public void checkin(Session session, JCRNodeWrapper node, Calendar checkinDate) throws RepositoryException {
        setNodeCheckinDate(node, checkinDate);
        session.getWorkspace().getVersionManager().checkin(node.getPath());
    }

    /**
     * Finds the closest version in a version history to a specific date.
     * @param vh the version history in which to lookup versions
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
            if (frozenNode.hasProperty("j:checkinDate")) {
                Property checkinDateProperty = frozenNode.getProperty("j:checkinDate");
                checkinDate = checkinDateProperty.getDate().getTime();
                checkinDateAvailable = true;
                if (checkinDate.compareTo(versionDate) > 0) {
                    closestVersion = lastVersion;
                    break;
                }
            } else {
                if (v.getCreated().getTime().compareTo(versionDate) > 0) {
                    // this can happen if we have a checkinDate, but try to resolve using the creation date.
                    closestVersion = lastVersion;
                    break;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Version " + v.getName() + " checkinDateAvailable=" + checkinDateAvailable + " checkinDate=" + checkinDate + " created=" + v.getCreated().getTime() + " properties:" + propertyString.toString());
            }
            lastVersion = v;
        }
        if (closestVersion == null && lastVersion != null) {
            // if we haven't found anything, maybe it's the last version that we should be using ?
            Date checkinDate;            
            Node frozenNode = lastVersion.getFrozenNode();
            if (frozenNode.hasProperty("j:checkinDate")) {
                Property checkinDateProperty = frozenNode.getProperty("j:checkinDate");
                checkinDate = checkinDateProperty.getDate().getTime();
                if (checkinDate.compareTo(versionDate) <= 0) {
                    closestVersion = lastVersion;
                }
            } else {
                if (lastVersion.getCreated().getTime().compareTo(versionDate) <= 0) {
                    closestVersion = lastVersion;
                }
            }
        }
        if (logger.isDebugEnabled()) {
            Date checkinDate = null;
            if (closestVersion.getFrozenNode().hasProperty("j:checkinDate")) {
                Property checkinDateProperty = closestVersion.getFrozenNode().getProperty("j:checkinDate");
                checkinDate = checkinDateProperty.getDate().getTime();
            }
            logger.debug("Resolved date " + versionDate + " for node title "+nodeTitle + " to closest version " + closestVersion.getName() + " createdTime=" + closestVersion.getCreated().getTime() + " checkinDate=" + checkinDate);
        }
        return closestVersion;
    }

}
