package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;

import javax.jcr.Node;
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
            long versionRevisionNumber = -1;
            if (versionNode.hasProperty("j:revisionNumber")) {
                versionRevisionNumber = versionNode.getProperty("j:revisionNumber").getLong();
            }
            VersionInfo versionInfo = new VersionInfo(v, versionRevisionNumber, "", 0);
            if (versionList.contains(versionInfo)) {
                versionList.remove(versionInfo); // we remove to keep only the latest one.
            }
            versionList.add(versionInfo);
        }
        return new ArrayList<VersionInfo>(versionList);
    }

    public long incrementRevisionNumber(Node node) throws RepositoryException {
        if (!node.isNodeType("jmix:versionInfo")) {
            return 0;
        }
        long currentRevision = 0;
        if (node.hasProperty("j:revisionNumber")) {
            currentRevision = node.getProperty("j:revisionNumber").getLong();
        }
        node.setProperty("j:revisionNumber", currentRevision+1);
        return currentRevision+1;
    }


    public void checkin(Session session, JCRNodeWrapper node) throws RepositoryException {
        incrementRevisionNumber(node);
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
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            if (v.getCreated().getTime().compareTo(versionDate) > 0) {
                closestVersion = lastVersion;
                break;
            } else if (v.getCreated().getTime().compareTo(versionDate) == 0) {
                closestVersion = v;
                break;
            }
            lastVersion = v;
        }
        return closestVersion;
    }

}
