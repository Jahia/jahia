import javax.jcr.*
import org.jahia.services.content.*

def log = log;

log.info("Start granting adminSiteTemplates permission to site-administrator role")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission("/permissions/admin/adminSiteTemplates", session).getPath(), "site-administrator", session)) {
            session.save();
            log.info("Permission granted")
        } else {
            log.info("Role already has the permission")
        }

        return null;
    }
});

log.info("... done granting adminSiteTemplates permission to site-administrator role.")