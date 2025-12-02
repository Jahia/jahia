import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.query.ScrollableQuery
import org.jahia.services.query.ScrollableQueryCallback
import javax.jcr.Node
import javax.jcr.query.Query

def log = log
def SCROLL_SIZE = 25

log.info("Migrating permissions from editMode/contributeMode to jContent structure")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

        // 1. Create workflow-dashboard-access permission
        createWorkflowDashboardPermission(session)

        // 2. Create jContent permission structure and migrate old permissions
        createJContentPermissionsAndMigrate(session)

        // 3. Delete legacy-managers node
        deleteLegacyManagers(session)

        // 4. Update roles and external permissions
        updatePermissionReferences(session)

        return null
    }

    void createWorkflowDashboardPermission(JCRSessionWrapper session) {
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions/workflow-tasks[not(workflow-dashboard-access/@jcr:primaryType='jnt:permission')]", Query.XPATH))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node workflowTasks = nodes.nextNode()

                    if (!workflowTasks.hasNode("workflow-dashboard-access")) {
                        workflowTasks.addNode("workflow-dashboard-access", "jnt:permission")
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

    void createJContentPermissionsAndMigrate(JCRSessionWrapper session) {
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("/jcr:root/permissions[not(jContent/@jcr:primaryType='jnt:permission')]", Query.XPATH))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node permissions = nodes.nextNode()

                    // Create jContent structure
                    if (!permissions.hasNode("jContent")) {
                        Node jContent = permissions.addNode("jContent", "jnt:permission")
                        jContent.addNode("jContentAccess", "jnt:permission")
                        jContent.addNode("jContentActions", "jnt:permission")
                    }

                    // Create legacy-permissions
                    if (!permissions.hasNode("legacy-permissions")) {
                        permissions.addNode("legacy-permissions", "jnt:permission")
                    }

                    // Migrate from editMode
                    if (permissions.hasNode("editMode")) {
                        Node editMode = permissions.getNode("editMode")

                        // Move createPageAction
                        if (editMode.hasNode("editModeActions")) {
                            Node editModeActions = editMode.getNode("editModeActions")
                            if (editModeActions.hasNode("createPageAction")) {
                                session.move(
                                    editModeActions.getNode("createPageAction").getPath(),
                                    "/permissions/jContent/jContentActions/createPageAction"
                                )
                            }
                        }

                        // Move useComponent and engineTabs
                        if (editMode.hasNode("useComponent")) {
                            JCRNodeWrapper useComponent = (JCRNodeWrapper) editMode.getNode("useComponent")
                            if (useComponent.hasNode("viewContributeModeTab")) {
                                JCRNodeWrapper viewContributeTab = (JCRNodeWrapper) useComponent.getNode("viewContributeModeTab")
                                viewContributeTab.rename("viewContentTypeRestrictionTab")
                            }
                            session.move(useComponent.getPath(), "/permissions/jContent/useComponent")
                        }

                        if (editMode.hasNode("engineTabs")) {
                            Node engineTabs = editMode.getNode("engineTabs")
                            if (engineTabs.hasNode("viewContributeModeTab")) {
                                session.move(
                                    engineTabs.getNode("viewContributeModeTab").getPath(),
                                    "/permissions/jContent/engineTabs/viewContentTypeRestrictionTab"
                                )
                            }
                            session.move(engineTabs.getPath(), "/permissions/jContent/engineTabs")
                        }

                        // Move editMode to legacy
                        session.move(editMode.getPath(), "/permissions/legacy-permissions/editMode")
                    }

                    // Move contributeMode to legacy
                    if (permissions.hasNode("contributeMode")) {
                        session.move(
                            permissions.getNode("contributeMode").getPath(),
                            "/permissions/legacy-permissions/contributeMode"
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

    void deleteLegacyManagers(JCRSessionWrapper session) {
        if (session.nodeExists("/permissions/legacy-managers")) {
            session.getNode("/permissions/legacy-managers").remove()
            session.save()
        }
    }

    void updatePermissionReferences(JCRSessionWrapper session) {
        // Update roles: editMode -> jContent
        updatePermissionInNodes(session, "[jnt:role]", ["editMode"], "jContent")
        updatePermissionInNodes(session, "[jnt:externalPermissions]", ["editMode"], "jContent")

        // Update roles: editModeAccess/contributeModeAccess/contributeMode -> jContentAccess
        updatePermissionInNodes(session, "[jnt:role]", ["editModeAccess", "contributeModeAccess", "contributeMode"], "jContentAccess")
        updatePermissionInNodes(session, "[jnt:externalPermissions]", ["editModeAccess", "contributeModeAccess", "contributeMode"], "jContentAccess")

        // Update roles: editModeActions -> jContentActions
        updatePermissionInNodes(session, "[jnt:role]", ["editModeActions"], "jContentActions")
        updatePermissionInNodes(session, "[jnt:externalPermissions]", ["editModeActions"], "jContentActions")

        // Remove obsolete selector permissions
        removePermissionsFromNodes(session, "[jnt:role]", [
            "editSelector", "categoriesSelector", "contentSelector", "createSelector",
            "filesAndImagesSelector", "latestSelector", "portletsSelector", "searchSelector",
            "sitemapSelector", "siteSettingsSelector"
        ])

        // Add workflow-dashboard-access to reviewer role
        addPermissionToRole(session, "reviewer", "workflow-dashboard-access")
    }

    void updatePermissionInNodes(JCRSessionWrapper session, String nodeType, List<String> oldPermissions, String newPermission) {
        def conditions = []
        if (oldPermissions) {
            oldPermissions.each { perm -> conditions.add("[j:permissionNames]='${perm}'") }
        }

        def queryStr = "SELECT * FROM ${nodeType} WHERE ${conditions.join(' OR ')}"
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery(queryStr, Query.JCR_SQL2))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node node = nodes.nextNode()

                    if (node.hasProperty("j:permissionNames")) {
                        def permissions = node.getProperty("j:permissionNames").getValues()
                        def permissionsList = permissions.collect { it.getString() } as List

                        // Remove old permissions
                        if (oldPermissions) {
                            permissionsList.removeAll(oldPermissions)
                        }

                        // Add new permission
                        if (!permissionsList.contains(newPermission)) {
                            permissionsList.add(newPermission)
                        }

                        node.setProperty("j:permissionNames", permissionsList as String[])
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

    void removePermissionsFromNodes(JCRSessionWrapper session, String nodeType, List<String> permissionsToRemove) {
        def conditions = permissionsToRemove.collect { "[j:permissionNames]='${it}'" }.join(' OR ')
        def queryStr = "SELECT * FROM ${nodeType} WHERE ${conditions}"
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery(queryStr, Query.JCR_SQL2))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node node = nodes.nextNode()

                    if (node.hasProperty("j:permissionNames")) {
                        def permissions = node.getProperty("j:permissionNames").getValues()
                        def permissionsList = permissions.collect { it.getString() } as List

                        permissionsList.removeAll(permissionsToRemove)
                        node.setProperty("j:permissionNames", permissionsList as String[])
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

    void addPermissionToRole(JCRSessionWrapper session, String roleName, String permission) {
        def query = new ScrollableQuery(SCROLL_SIZE, session.getWorkspace().getQueryManager()
                .createQuery("SELECT * FROM [jnt:role] WHERE LOCALNAME() = '${roleName}'", Query.JCR_SQL2))

        query.execute(new ScrollableQueryCallback<Void>() {
            @Override
            boolean scroll() throws RepositoryException {
                def nodes = stepResult.getNodes()
                boolean hasNodes = false

                while (nodes.hasNext()) {
                    hasNodes = true
                    Node role = nodes.nextNode()

                    if (role.hasProperty("j:permissionNames")) {
                        def permissions = role.getProperty("j:permissionNames").getValues()
                        def permissionsList = permissions.collect { it.getString() } as List

                        if (!permissionsList.contains(permission)) {
                            permissionsList.add(permission)
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
    }
})
