import org.jahia.osgi.FrameworkService
import org.jahia.registries.ServicesRegistry
import org.jahia.settings.SettingsBean
import org.osgi.framework.FrameworkUtil

import javax.jcr.*
import org.apache.log4j.Logger
import org.jahia.services.content.*

import javax.jcr.query.Query

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

File cmmConfig = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/org.jahia.modules.api.permissions-cmm.cfg");
if (cmmConfig.exists()) {
    log.info("Removing old configuration");
    cmmConfig.delete();
}

FrameworkService.getBundleContext().getBundles().each { bundle ->
    if (bundle.getSymbolicName().equals("content-media-manager")) {
        log.info("Uninstall content-media-manager");
        bundle.uninstall();
    }
}

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        def value = session.getValueFactory().createValue("contentManager")
        def it = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:role] where [j:permissionNames]='contentManager'", Query.JCR_SQL2).execute().getNodes();
        while (it.hasNext()) {
            def node = it.nextNode()
            log.info("Update permissions for "+node.getName());
            node.getProperty("j:permissionNames").removeValue(value);
        }
        session.save();

        return null
    }
})

