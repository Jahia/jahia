import java.util.*
import javax.jcr.*
import org.jahia.services.content.*

if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

def log = log;

log.info("Start modifying contribute mode permissions...")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        boolean doSave = false;
        if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission("/permissions/editMode/engineTabs/viewContributeModeTab", session).getPath(), "editor", session)) {
            doSave = true;
            log.info("Permission granted")
        } else {
            log.info("Role already has the permission")
        }

        if (RBACUtils.grantPermissionToRole(RBACUtils.getOrCreatePermission("/permissions/editMode/editModeActions", session).getPath(), "editor", session)) {
            doSave = true;
            log.info("Permission granted")
        } else {
            log.info("Role already has the permission")
        }

        if (doSave) {
            session.save();
        }
        return null;
    }
});

log.info("... done modifying contribute mode permissions.")