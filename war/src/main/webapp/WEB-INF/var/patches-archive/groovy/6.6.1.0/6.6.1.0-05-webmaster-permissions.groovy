import javax.jcr.*
import org.jahia.services.content.*


if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

def log = log;

log.info("Start creating permissions for components usage")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper role = session.getNode("/roles/web-designer");

        JCRNodeWrapper ext;
        ext = role.addNode("studio-access","jnt:externalPermissions")
        ext.setProperty("j:path","/templateSets")
        javax.jcr.Value[] v = new javax.jcr.Value[3];
        v[0] = session.getValueFactory().createValue(session.getNode("/permissions/editMode"),true);
        v[1] = session.getValueFactory().createValue(session.getNode("/permissions/studioMode"),true);
        v[2] = session.getValueFactory().createValue(session.getNode("/permissions/repository-permissions"),true);
        ext.setProperty("j:permissions", v)

        v = new javax.jcr.Value[1];
        v[0] = session.getValueFactory().createValue("jnt:virtualsite");
        role.setProperty("j:nodeTypes", v);

        session.save();
        return null;
    }
});

log.info("... done creating permissions for components usage.")