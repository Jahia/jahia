import javax.jcr.*
import org.jahia.services.content.*


if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

def log = log;

log.info("Start creating permissions for components usage")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        RBACUtils.getOrCreatePermission("/permissions/editMode/useComponent/useComponentForCreate", session);
        RBACUtils.getOrCreatePermission("/permissions/editMode/useComponent/useComponentForEdit", session);

        session.save();

        return null;
    }
});

log.info("... done creating permissions for components usage.")