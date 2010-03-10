package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;

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

    private static transient Logger logger = Logger.getLogger(JCRPublicationService.class);

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
                VersionInfo versionInfo = new VersionInfo(v, versionRevisionNumber, "", false, v.getCreated().getTime());
                if (versionList.contains(versionInfo)) {
                    versionList.remove(versionInfo); // we remove to keep only the latest one.
                }
                versionList.add(versionInfo);
            }
        }
        return new ArrayList<VersionInfo>(versionList);
    }

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
}
