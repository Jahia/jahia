import org.apache.log4j.Logger
import org.jahia.services.content.*
import org.jahia.services.query.QueryResultWrapper

import javax.jcr.RepositoryException
import javax.jcr.query.Query

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

def callback = new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrSession) throws RepositoryException {
        def migrateMountPoints = { String nodeType ->
            QueryResultWrapper result = jcrSession.getWorkspace().getQueryManager().createQuery("select * from [" + nodeType + "]", Query.JCR_SQL2).execute();
            JCRNodeIteratorWrapper ni = result.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper mountPointNode = (JCRNodeWrapper) ni.next();
                try {
                    String mountPointProvider = null;
                    if (mountPointNode.hasProperty("j:provider")) {
                        JCRPropertyWrapper providerProperty = mountPointNode.getProperty("j:provider");
                        mountPointProvider = providerProperty.getString();
                    } else {
                        log.warn("No j:provider property found for mount point node " + mountPointNode.getPath());
                    }
                    String mountPointRoot = null;
                    if (mountPointNode.hasProperty("j:root")) {
                        mountPointRoot = mountPointNode.getProperty("j:root").getString();
                    } else {
                        log.warn("No j:root property found for mount point node " + mountPointNode.getPath());
                    }
                    String newNodeType = null;
                    if ("vfs".equals(mountPointProvider)) {
                        newNodeType = "jnt:vfsMountPoint";
                    }
                    if (newNodeType != null) {
                        jcrSession.checkout(mountPointNode);
                        String mountPointName = mountPointNode.getName();
                        JCRNodeWrapper parentNode = mountPointNode.getParent();
                        jcrSession.checkout(parentNode);
                        mountPointNode.remove();
                        JCRNodeWrapper newMountPointNode = parentNode.addNode(mountPointName, newNodeType);
                        if ("vfs".equals(mountPointProvider)) {
                            newMountPointNode.setProperty("j:rootPath", mountPointRoot);
                        }
                        jcrSession.save();
                    }
                } catch (Exception e) {
                    log.error("Failed to change references to names", e);
                }
            }
        }
        def workspaceName = jcrSession.getWorkspace().getName();
        log.info("Updating mount points to new node types " + workspaceName + "...");
        migrateMountPoints("jnt:mountPoint");
        log.info("...update done.")
    }
};
JCRTemplate.getInstance().doExecuteWithSystemSession(callback);
