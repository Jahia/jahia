import javax.jcr.*
import org.apache.log4j.Logger
import org.jahia.services.content.*

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        // Add permission
        JCRNodeWrapper n = session.getNode("/permissions/repository-permissions");
        if (!n.hasNode("clearLock")) {
            n.addNode("clearLock", "jnt:permission")
            session.save()
            log.info("clearLock permission added.")
        }

        return null
    }
})
