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

log.info("Adding page tree permissions and updating roles")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

        // 1. Add pageTreeActions and new jContent action permissions
        createPageTreePermissions(session)

        // 2. Add pageComposerAccess permission
        createPageComposerAccessPermission(session)

        // 3. Update roles with pageComposerAccess
        updateRoleWithPermission(session, "translator", "currentSite-access", "pageComposerAccess")
        updateRoleWithPermission(session, "editor", "currentSite-access", "pageComposerAccess")
        updateRoleWithPermission(session, "reviewer", "currentSite-access", "pageComposerAccess")

        // 4. Update translator role with translateAction
        updateRoleWithPermission(session, "translator", "currentSite-access", "translateAction")

        // 5. Update roles with jContentActions
        updateRoleWithPermission(session, "reviewer", "currentSite-access", "jContentActions")
        updateRoleWithPermission(session, "translator", "currentSite-access", "jContentActions")

        return null
    }

    void createPageTreePermissions(JCRSessionWrapper session) {
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions/jContent/jContentActions[not(pageTreeActions/@jcr:primaryType='jnt:permission')]", Query.XPATH))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node jContentActions = nodes.nextNode()

                    // Create pageTreeActions with child permissions
                    if (!jContentActions.hasNode("pageTreeActions")) {
                        Node pageTreeActions = jContentActions.addNode("pageTreeActions", "jnt:permission")
                        pageTreeActions.addNode("orderPagesAction", "jnt:permission")
                        pageTreeActions.addNode("editPageAction", "jnt:permission")
                        pageTreeActions.addNode("copyPageAction", "jnt:permission")
                        pageTreeActions.addNode("cutPageAction", "jnt:permission")
                        pageTreeActions.addNode("pastePageAction", "jnt:permission")
                        pageTreeActions.addNode("pasteOnePageAction", "jnt:permission")
                        pageTreeActions.addNode("lockPageAction", "jnt:permission")
                        pageTreeActions.addNode("newInternalLinkAction", "jnt:permission")
                        pageTreeActions.addNode("newExternalLinkAction", "jnt:permission")
                        pageTreeActions.addNode("newMenuLabelAction", "jnt:permission")
                        pageTreeActions.addNode("deletePageAction", "jnt:permission")
                        pageTreeActions.addNode("exportPageAction", "jnt:permission")
                        pageTreeActions.addNode("importPageAction", "jnt:permission")
                    }

                    // Add new jContent action permissions
                    if (!jContentActions.hasNode("editAction")) {
                        jContentActions.addNode("editAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("copyAction")) {
                        jContentActions.addNode("copyAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("cutAction")) {
                        jContentActions.addNode("cutAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("pasteAction")) {
                        jContentActions.addNode("pasteAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("exportAction")) {
                        jContentActions.addNode("exportAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("importAction")) {
                        jContentActions.addNode("importAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("translateAction")) {
                        jContentActions.addNode("translateAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("newContentFolderAction")) {
                        jContentActions.addNode("newContentFolderAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("newMediaFolderAction")) {
                        jContentActions.addNode("newMediaFolderAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("zipAction")) {
                        jContentActions.addNode("zipAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("unzipAction")) {
                        jContentActions.addNode("unzipAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("uploadFilesAction")) {
                        jContentActions.addNode("uploadFilesAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("replaceWithAction")) {
                        jContentActions.addNode("replaceWithAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("openImageEditorAction")) {
                        jContentActions.addNode("openImageEditorAction", "jnt:permission")
                    }
                    if (!jContentActions.hasNode("downloadAction")) {
                        jContentActions.addNode("downloadAction", "jnt:permission")
                    }

                    // Move createPageAction to pageTreeActions
                    if (jContentActions.hasNode("createPageAction")) {
                        session.move(
                                jContentActions.getNode("createPageAction").getPath(),
                                "/permissions/jContent/jContentActions/pageTreeActions/createPageAction"
                        )
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
    }

    void createPageComposerAccessPermission(JCRSessionWrapper session) {
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions/jContent[not(pageComposerAccess/@jcr:primaryType='jnt:permission')]", Query.XPATH))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node jContent = nodes.nextNode()

                    if (!jContent.hasNode("pageComposerAccess")) {
                        jContent.addNode("pageComposerAccess", "jnt:permission")
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
    }

    void updateRoleWithPermission(JCRSessionWrapper session, String roleName, String accessNode, String permissionName) {
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("SELECT * FROM [jnt:role] WHERE LOCALNAME() = '${roleName}'", Query.JCR_SQL2))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def roleNodes = stepResult.getNodes()
                boolean hasNodes = false

                while (roleNodes.hasNext()) {
                    hasNodes = true
                    Node role = roleNodes.nextNode()

                    if (role.hasNode(accessNode)) {
                        Node access = role.getNode(accessNode)

                        if (access.hasProperty("j:permissionNames")) {
                            def permissions = access.getProperty("j:permissionNames").getValues()
                            def permissionsList = permissions.collect { it.getString() } as List

                            if (!permissionsList.contains(permissionName)) {
                                permissionsList.add(permissionName)
                                access.setProperty("j:permissionNames", permissionsList as String[])
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
    }
})
