import javax.jcr.RepositoryException
import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.content.RBACUtils

def log = log;

log.info("Start granting some tabs permission to reviewer role")

def permissions = ["/permissions/editMode/engineTabs/viewContentTab", "/permissions/editMode/engineTabs/viewMetadataTab",
        "/permissions/editMode/engineTabs/viewCategoriesTab", "/permissions/editMode/engineTabs/viewTagsTab",
        "/permissions/editMode/engineTabs/viewSeoTab"]

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        permissions.each() { permission ->
            if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission(permission, session).getPath(), "reviewer", session)) {
                log.info("Permission ${permission} granted to reviewer")
            } else {
                log.info("Role reviewer already has the permission ${permission}")
            }
        }
        session.save();
        return null;
    }
});

log.info("... done granting tabs permission to reviewer role.")

log.info("Start granting some tabs permission to contributor role")

permissions = ["/permissions/editMode/engineTabs/viewContentTab", "/permissions/editMode/engineTabs/viewMetadataTab",
        "/permissions/editMode/engineTabs/viewCategoriesTab", "/permissions/editMode/engineTabs/viewTagsTab"]

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        permissions.each() { permission ->
            if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission(permission, session).getPath(), "contributor", session)) {
                log.info("Permission ${permission} granted to contributor")
            } else {
                log.info("Role contributor already has the permission ${permission}")
            }
        }
        session.save();
        return null;
    }
});

log.info("... done granting tabs permission to contributor role.")