import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate

import javax.jcr.RepositoryException

// This migration script revokes the jcr:modifyAccessControl_default permission from the /roles/editor role.
//
// Prior to this patch, the editor role had j:permissionNames = "jcr:all_default" which implicitly
// granted jcr:modifyAccessControl_default (ACL management).
//
// The script handles two scenarios:
//   1. jcr:all_default is present: expand it into all its child permissions EXCEPT jcr:modifyAccessControl_default
//   2. jcr:modifyAccessControl_default is explicitly present: simply remove it
//
// If neither jcr:all_default nor jcr:modifyAccessControl_default is found in the current permissions,
// the role is considered already safe and no changes are made.

log.info("Revoking jcr:modifyAccessControl_default from editor role")

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

        // The expanded set of permissions that replace jcr:all_default, excluding jcr:modifyAccessControl_default.
        // These correspond to the children of jcr:all_default in root-permissions.xml minus jcr:modifyAccessControl_default.
        def expandedPermissions = [
                "api-access",
                "jcr:read_default",
                "jcr:write_default",
                "jcr:readAccessControl_default",
                "jcr:lockManagement_default",
                "jcr:versionManagement_default",
                "jcr:nodeTypeManagement_default",
                "jcr:retentionManagement_default",
                "jcr:lifecycleManagement_default"
        ]

        // Check that the editor role exists
        if (!session.nodeExists("/roles/editor")) {
            log.info("Role /roles/editor does not exist, skipping migration")
            return null
        }

        def editorRole = session.getNode("/roles/editor")

        // Check that the role has the j:permissionNames property
        if (!editorRole.hasProperty("j:permissionNames")) {
            log.info("Role /roles/editor has no j:permissionNames property, skipping migration")
            return null
        }

        // Read current permission values into a mutable list
        def currentValues = editorRole.getProperty("j:permissionNames").getValues()
        def permissionsList = currentValues.collect { it.getString() } as List

        boolean modified = false

        if (permissionsList.contains("jcr:all_default")) {
            // Case 1: jcr:all_default is present — expand it into individual child permissions
            // (excluding jcr:modifyAccessControl_default), and preserve any other custom permissions
            log.info("Found jcr:all_default in editor role, expanding to individual permissions without jcr:modifyAccessControl_default")
            permissionsList.remove("jcr:all_default")

            // Add expanded permissions, avoiding duplicates
            expandedPermissions.each { perm ->
                if (!permissionsList.contains(perm)) {
                    permissionsList.add(perm)
                }
            }
            modified = true
        }

        if (permissionsList.contains("jcr:modifyAccessControl_default")) {
            // Case 2: jcr:modifyAccessControl_default is explicitly listed — just remove it
            log.info("Found jcr:modifyAccessControl_default in editor role, removing it")
            permissionsList.remove("jcr:modifyAccessControl_default")
            modified = true
        }

        if (modified) {
            // Update the property with the new permissions list
            editorRole.setProperty("j:permissionNames", permissionsList as String[])
            session.save()
            log.info("Successfully updated j:permissionNames on /roles/editor: ${permissionsList}")
        } else {
            log.info("Role /roles/editor does not contain jcr:all_default or jcr:modifyAccessControl_default, no changes needed")
        }

        return null
    }
})
