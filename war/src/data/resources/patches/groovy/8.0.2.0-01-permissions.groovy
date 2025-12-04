import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.query.ScrollableQuery
import org.jahia.services.query.ScrollableQueryCallback
import javax.jcr.Node
import javax.jcr.query.Query
import javax.jcr.RepositoryException

def log = log
def SCROLL_SIZE = 25

log.info("Moving workflow-dashboard-access permission and creating developerTools permissions")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

        // Move workflow-dashboard-access if it exists
        def workflowPermQuery = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions/workflow-tasks/workflow-dashboard-access", Query.XPATH))

        workflowPermQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def permNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (permNodes.hasNext()) {
                    hasNodes = true
                    Node permNode = permNodes.nextNode()
                    String targetPath = "/permissions/managers/workflow-dashboard-access"
                    if (!session.nodeExists(targetPath)) {
                        session.move(permNode.getPath(), targetPath)
                    } else {
                        log.warn("Target node already exists at {}, skipping move for {}", targetPath, permNode.getPath())
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

        // Create developerTools permission structure
        def permissionsQuery = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions[not(developerTools/@jcr:primaryType='jnt:permission')]", Query.XPATH))

        permissionsQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def permissionNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (permissionNodes.hasNext()) {
                    hasNodes = true
                    Node permissionsNode = permissionNodes.nextNode()

                    if (!permissionsNode.hasNode("developerTools")) {
                        Node developerTools = permissionsNode.addNode("developerTools", "jnt:permission")
                        developerTools.addNode("developerToolsAccess", "jnt:permission")
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

        // Update web-designer role
        def rolesQuery = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("SELECT * FROM [jnt:role] WHERE LOCALNAME() = 'web-designer'", Query.JCR_SQL2))

        rolesQuery.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def roleNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (roleNodes.hasNext()) {
                    hasNodes = true
                    Node role = roleNodes.nextNode()

                    if (role.hasProperty("j:permissionNames")) {
                        def permissions = role.getProperty("j:permissionNames").getValues()
                        def permissionsList = permissions.collect { it.getString() } as List

                        if (!permissionsList.contains("developerToolsAccess")) {
                            permissionsList.add("developerToolsAccess")
                            role.setProperty("j:permissionNames", permissionsList as String[])
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
