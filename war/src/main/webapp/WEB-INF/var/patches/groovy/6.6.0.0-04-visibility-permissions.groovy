import javax.jcr.*
import org.jahia.services.content.*

def log = log;

log.info("Start granting viewVisibilityTab permission to editor role")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission("/permissions/editMode/engineTabs/viewVisibilityTab", session).getPath(), "editor", session)) {
            session.save();
            log.info("Permission granted")
        } else {
            log.info("Role already has the permission")
        }

        return null;
    }
});

log.info("... done granting viewVisibilityTab permission to editor role.")