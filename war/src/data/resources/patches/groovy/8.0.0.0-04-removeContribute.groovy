import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.query.ScrollableQuery
import org.jahia.services.query.ScrollableQueryCallback
import javax.jcr.Node
import javax.jcr.query.Query

def log = log

log.info("Update site nodes to remove contribute, content-media-manager and dx-commons-webpack modules")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        def scrollableQuery = new ScrollableQuery(25, session.getWorkspace().getQueryManager()
                .createQuery("SELECT * FROM [jnt:virtualsite] WHERE ISCHILDNODE('/sites')", Query.JCR_SQL2))

        scrollableQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def siteNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (siteNodes.hasNext()) {
                    hasNodes = true
                    Node site = siteNodes.nextNode()
                    if (site.hasProperty("j:installedModules")) {
                        def modules = site.getProperty("j:installedModules").getValues()
                        def newModules = modules.findAll { value ->
                            !["contribute", "content-media-manager", "dx-commons-webpack"].contains(value.getString())
                        }
                        site.setProperty("j:installedModules", newModules as String[])
                    }
                }

                session.save()
                return hasNodes
            }

            @Override
            protected Void getResult() {
                return null
            }
        })

        return null
    }
})
