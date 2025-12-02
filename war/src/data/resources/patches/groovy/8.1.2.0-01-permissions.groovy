import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.query.ScrollableQuery
import org.jahia.services.query.ScrollableQueryCallback
import javax.jcr.Node
import javax.jcr.query.Query

def log = log

log.info("Add viewContentTab permission to translator role's currentSite-access")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        def scrollableQuery = new ScrollableQuery(25, session.getWorkspace().getQueryManager()
                .createQuery("SELECT * FROM [jnt:role] WHERE LOCALNAME() = 'translator'", Query.JCR_SQL2))

        scrollableQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def roleNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (roleNodes.hasNext()) {
                    hasNodes = true
                    Node role = roleNodes.nextNode()

                    if (role.hasNode("currentSite-access")) {
                        Node accessNode = role.getNode("currentSite-access")

                        if (accessNode.hasProperty("j:permissionNames")) {
                            def permissions = accessNode.getProperty("j:permissionNames").getValues()
                            def permissionsList = permissions.collect { it.getString() } as List

                            if (!permissionsList.contains("viewContentTab")) {
                                permissionsList.add("viewContentTab")
                                accessNode.setProperty("j:permissionNames", permissionsList as String[])
                            }
                        }
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
