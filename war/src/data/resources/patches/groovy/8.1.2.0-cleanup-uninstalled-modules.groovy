import org.apache.log4j.Logger
import org.jahia.osgi.BundleUtils
import org.jahia.services.SpringContextSingleton
import org.jahia.services.content.JCRTemplate
import org.jahia.services.modulemanager.persistence.BundlePersister
import org.jahia.settings.SettingsBean

import javax.jcr.query.Query
import javax.jcr.query.QueryResult

final Logger logger = Logger.getLogger("org.jahia.tools.groovyConsole");

// Run on processing node only
if (SettingsBean.getInstance().isClusterActivated() && !SettingsBean.getInstance().isProcessingServer()) {
    return;
}

BundlePersister persister = (BundlePersister) SpringContextSingleton
        .getBean("org.jahia.services.modulemanager.persistence.BundlePersister");

JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
    Query query = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jnt:moduleManagementBundle]", Query.JCR_SQL2);
    QueryResult result = query.execute();
    result.getNodes().forEach(node -> {
        try {
            if (BundleUtils.getBundle(node.getProperty("j:symbolicName").getString(), node.getProperty("j:version").getString()) == null) {
                String bundleKey = node.getProperty("j:groupId").getString() + "/" + node.getProperty("j:symbolicName").getString() + "/" + node.getProperty("j:version").getString();
                if (persister.delete(bundleKey)) {
                    logger.info("Remove " + bundleKey);
                } else {
                    logger.warn("Unable to remove bundle entry " + bundleKey + " for path " + node.getPath() + " - please remove the node manually");
                }
            } else {
                logger.info(String.format("Module %s/%s found", node.getProperty("j:symbolicName").getString(), node.getProperty("j:version").getString()));
            }
        } catch (Exception e) {
            logger.error("Unable to remove bundle entry for path " + node.getPath() + " - please remove the node manually", e);
        }
    })
})