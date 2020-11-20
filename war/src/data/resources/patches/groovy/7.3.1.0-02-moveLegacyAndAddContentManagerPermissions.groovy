import javax.jcr.*;
import javax.jcr.query.*;

import org.jahia.services.content.*;

import java.util.stream.Collectors;

def log = log;

private static void createLegacyPermissionNode() {
    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
            JCRNodeWrapper perm = session.getNode("/permissions");
            if (!perm.hasNode("legacy-managers")) {
                perm.addNode("legacy-managers", "jnt:permission");
                session.save();
            }
            return null;
        }
    })
};

private static void moveNode(String path, String targetPath) {
    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
            if (session.nodeExists(path)) {
                session.move(path, targetPath);
                session.save();
            }
            return null;
        }
    })

};

private void adjustExternalPermissions() {
    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
            QueryResult qr = session
                    .getWorkspace()
                    .getQueryManager()
                    .createQuery("select * from [jnt:externalPermissions] where [j:permissionNames]='editorialContentManager' or  [j:permissionNames]='fileManager' or  [j:permissionNames]='siteManager'", Query.JCR_SQL2)
                    .execute();

            for (JCRNodeIteratorWrapper nodeIt = qr.getNodes(); nodeIt.hasNext();) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodeIt.next();
                JCRPropertyWrapper prop = node.getProperty("j:permissionNames");
                Value[] values = Arrays.stream(prop.getValues())
                        .filter({ v ->
                            try {
                                String perm = v.getString();
                                if ("editorialContentManager".equals(perm) ||  "fileManager".equals(perm) || "siteManager".equals(perm)) {
                                    return true;
                                }
                            } catch (Exception e) {
                                log.error("Could not read value: {}", e.getMessage(), e);
                            }
                            return false;
                        })
                        .collect(Collectors.toList()).toArray() as Value[];

                if (values.length > 0) {
                    prop.removeValues(values);
                    session.save();
                }

                if (Arrays.stream(prop.getValues()).noneMatch({ v -> "contentManager".equals(v.getString())})) {
                    prop.addValue("contentManager");
                    session.save();
                }
            };
            return null;
        }
    })

};


log.info("Start moving legacy permissions ...")

createLegacyPermissionNode();
moveNode("/permissions/managers/editorialContentManager", "/permissions/legacy-managers/editorialContentManager");
moveNode("/permissions/managers/fileManager", "/permissions/legacy-managers/fileManager");
moveNode("/permissions/managers/siteManager", "/permissions/legacy-managers/siteManager");
adjustExternalPermissions();

log.info("...done moving legacy permissions.")
