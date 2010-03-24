package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            Node frozenNode = v.getFrozenNode();
            Date checkinDate = null;
            if (frozenNode.hasProperty("j:checkinDate")) {
                Property checkinDateProperty = frozenNode.getProperty("j:checkinDate");
                checkinDate = checkinDateProperty.getDate().getTime();
            } else {
                checkinDate = v.getCreated().getTime();
            }
            if (checkinDate.compareTo(versionDate) > 0) {
                closestVersion = lastVersion;
                break;
            } else if (checkinDate.compareTo(versionDate) == 0) {
                closestVersion = v;
                break;
            } else if (v.getCreated().getTime().compareTo(versionDate) > 0) {
                // this can happen if we have a checkinDate, but try to resolve using the creation date.
                closestVersion = lastVersion;
                break;
            } else if (v.getCreated().getTime().compareTo(versionDate) == 0) {
                closestVersion = v;
                break;                
            }
            lastVersion = v;
        }
        if (logger.isDebugEnabled()) {
            Date checkinDate = null;
            if (closestVersion.getFrozenNode().hasProperty("j:checkinDate")) {
                Property checkinDateProperty = closestVersion.getFrozenNode().getProperty("j:checkinDate");
                checkinDate = checkinDateProperty.getDate().getTime();
            }
            logger.debug("Resolved date " + versionDate + " to closest version " + closestVersion.getName() + " createdTime=" + closestVersion.getCreated().getTime() + " checkinDate=" + checkinDate);
        }
        return closestVersion;
    }

}
