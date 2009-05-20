package org.jahia.ajax.gwt.filemanagement.server.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRVersionHistory;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 2, 2009
 * Time: 7:03:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRVersioningHelper {
    private static JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
    private static Logger logger = Logger.getLogger(JCRVersioningHelper.class);

    public static void activateVersioning(List<String> pathes, ProcessingContext jParams) {
        for (String path : pathes) {
            try {
                JCRSessionWrapper s = jcr.getThreadSession(jParams.getUser());
                JCRNodeWrapper node = s.getNode(path);
                if (!node.isVersioned()) {
                    node.versionFile();
                    s.save();
                }
            } catch (Throwable e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public static List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node, ProcessingContext jParams) {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        try {
            VersionHistory vh = node.getVersionHistory();
            VersionIterator vi = vh.getAllVersions();
            while (vi.hasNext()) {
                Version v = vi.nextVersion();
                if (!v.getName().equals("jcr:rootVersion")) {
                    JCRNodeWrapper orig = ((JCRVersionHistory) v.getContainingHistory()).getNode();
                    GWTJahiaNode n = FileManagerWorker.getGWTJahiaNode(orig);
                    n.setUrl(orig.getUrl()+"?v="+v.getName());
                    GWTJahiaNodeVersion jahiaNodeVersion = new GWTJahiaNodeVersion(v.getName(), v.getCreated().getTime());
                    jahiaNodeVersion.setNode(n);
                    versions.add(jahiaNodeVersion);
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return versions;
    }

}
