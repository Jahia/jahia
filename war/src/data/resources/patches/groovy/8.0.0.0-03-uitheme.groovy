import org.apache.log4j.Logger
import javax.jcr.*
import org.jahia.services.content.*
import javax.jcr.query.Query

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        // Add permission
        def nodes = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jnt:user] where [jahia.ui.theme] is not null", Query.JCR_SQL2).execute().getNodes();
        def count = 0;
        while (nodes.hasNext()) {
            count++;
            JCRNodeWrapper node = nodes.nextNode();
            try {
                // External node users will be part of the results as most do not implement search
                if (node.hasProperty("jahia.ui.theme")) {
                    node.getProperty("jahia.ui.theme").remove();
                }
            } catch (PathNotFoundException e) {
                // Node might be external and not test the property correctly
            }
            //to prevent session from overflowing for big projects saving session for each 100 users
            if (count % 100 == 0) {
                log.info("Removing theme for 100 users.");
                session.save();
            }
        }
        session.save();
        return null;
    }
})
