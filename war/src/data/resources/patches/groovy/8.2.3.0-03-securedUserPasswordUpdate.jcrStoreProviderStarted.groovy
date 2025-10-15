import javax.jcr.*
import org.jahia.services.content.*

log.info("Migration script to add unsecure permission setUsersPassword for password update")

// Create the setUsersPassword permission to allow password updates without current password verification (for admins)
JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        // Look for the root node with the permissions
        if (!session.nodeExists("/permissions/unsecure-permissions")) {
            log.info("Add '/permissions/unsecure-permissions' node")
            session.getNode("/permissions").addNode("unsecure-permissions", "jnt:permission")
        } else {
            log.info("Node '/permissions/unsecure-permissions' already exists")
        }

        // Check 'setUsersPassword' permission
        if (!session.nodeExists("/permissions/unsecure-permissions/setUsersPassword")) {
            log.info("Add 'setUsersPassword' permissions to 'unsecure-permissions' node")
            session.getNode("/permissions/unsecure-permissions").addNode("setUsersPassword", "jnt:permission")
        } else {
            log.info("Permission 'setUsersPassword' already exists")
        }

        log.info("Save changes")
        session.save()

        return null
    }
})

log.info("End of setUsersPassword set up")
