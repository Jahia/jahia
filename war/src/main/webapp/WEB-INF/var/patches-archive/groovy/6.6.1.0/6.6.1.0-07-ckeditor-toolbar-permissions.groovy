import javax.jcr.*

import org.jahia.services.content.*


if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

def log = log;

log.info("Start granting CKEditor Basic toolbar permission to contributor and editor-in-chief roles...")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        boolean doSave = false;
        if (RBACUtils.revokePermissionFromRole("/permissions/wysiwyg-editor-toolbar/view-light-wysiwyg-editor", "editor-in-chief", session)) {
            doSave = RBACUtils.grantPermissionToRole("/permissions/wysiwyg-editor-toolbar/view-basic-wysiwyg-editor", "editor-in-chief", session);
            if (doSave) {
                log.info("Permission granted")
            }
        } else {
            JCRNodeWrapper role = session.getNode("/roles/editor-in-chief");
            boolean found = false;
            for (Value val : role.getProperty("j:permissions").getValues()) {
                JCRNodeWrapper perm = ((JCRValueWrapper) val).getNode();
                if (perm != null && perm.getPath().startsWith("/permissions/wysiwyg-editor-toolbar")) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                doSave = RBACUtils.grantPermissionToRole("/permissions/wysiwyg-editor-toolbar/view-basic-wysiwyg-editor", "editor-in-chief", session);
            } else {
                log.info("Role editor-in-chief already has toolbar permission");
            }
        }
        
        JCRNodeWrapper role = session.getNode("/roles/contributor");
        boolean found = false;
        for (Value val : role.getProperty("j:permissions").getValues()) {
            JCRNodeWrapper perm = ((JCRValueWrapper) val).getNode();
            if (perm != null && perm.getPath().startsWith("/permissions/wysiwyg-editor-toolbar")) {
                found = true;
                break;
            }
        }
        if (!found) {
            doSave = RBACUtils.grantPermissionToRole("/permissions/wysiwyg-editor-toolbar/view-basic-wysiwyg-editor", "contributor", session);
        } else {
            log.info("Role contributor already has toolbar permission");
        }

        if (doSave) {
            session.save();
        }
        return null;
    }
});

log.info("... done granting CKEditor toolbar permissions.")