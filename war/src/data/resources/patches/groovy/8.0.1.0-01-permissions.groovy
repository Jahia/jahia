import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.query.ScrollableQuery
import org.jahia.services.query.ScrollableQueryCallback
import javax.jcr.Node
import javax.jcr.query.Query

def log = log
def SCROLL_SIZE = 25

log.info("Creating system tools permissions and system-administrator role")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

        // Create systemTools permission structure
        def permissionsQuery = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions[not(systemTools/@jcr:primaryType='jnt:permission')]", Query.XPATH))

        permissionsQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def permissionNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (permissionNodes.hasNext()) {
                    hasNodes = true
                    Node permissionsNode = permissionNodes.nextNode()

                    if (!permissionsNode.hasNode("systemTools")) {
                        Node systemTools = permissionsNode.addNode("systemTools", "jnt:permission")
                        systemTools.addNode("systemToolsAccess", "jnt:permission")
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

        // Create system-administrator role
        def rolesQuery = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/roles[not(system-administrator/@jcr:primaryType='jnt:role')]", Query.XPATH))

        rolesQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def roleNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (roleNodes.hasNext()) {
                    hasNodes = true
                    Node rolesNode = roleNodes.nextNode()

                    if (!rolesNode.hasNode("system-administrator")) {
                        Node role = rolesNode.addNode("system-administrator", "jnt:role")
                        role.setProperty("j:hidden", false)
                        role.setProperty("j:privilegedAccess", false)
                        role.setProperty("j:roleGroup", "system-role")
                        role.setProperty("j:permissionNames", ["systemToolsAccess", "repository-permissions"] as String[])

                        Node access = role.addNode("rootNode-access", "jnt:externalPermissions")
                        access.setProperty("j:path", "/")
                        access.setProperty("j:permissionNames", ["systemToolsAccess"] as String[])
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

        // Create tools node structure
        def rootQuery = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root[not(tools/@jcr:primaryType='nt:base')]", Query.XPATH))

        rootQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def rootNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (rootNodes.hasNext()) {
                    hasNodes = true
                    Node rootNode = rootNodes.nextNode()

                    if (!rootNode.hasNode("tools")) {
                        Node tools = rootNode.addNode("tools", "nt:unstructured")
                        tools.addMixin("jmix:accessControlled")

                        Node acl = tools.addNode("j:acl", "jnt:acl")
                        acl.setProperty("j:inherit", false)
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
