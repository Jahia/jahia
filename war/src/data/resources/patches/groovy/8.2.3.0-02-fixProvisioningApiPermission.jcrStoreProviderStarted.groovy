import javax.jcr.*
import org.jahia.services.content.*

def addValue = (JCRPropertyWrapper property, String value) -> {
    if (!property.getValues().find(val -> val.getString().equals(value))) {
        property.addValue(value)
    }
}

log.info("Migration script to add or clean up provisioningAccess permission on root-access role")
JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        // Add missing permission if needed
        log.info("Add provisioningAccess permission")
        if (!session.nodeExists("/permissions/provisioningApi")) {
            sesion.getNode("/permissions").addNode("provisioningApi", "jnt:permission")
        }
        if (!session.nodeExists("/permissions/provisioningApi/provisioningAccess")) {
            session.getNode("/permissions/provisioningApi").addNode("provisioningAccess", "jnt:permission")
        }
        session.save()
        // Set permission on system administrator
        log.info("Set permission on  /roles/system-administrator")
        if (session.nodeExists("/roles/system-administrator")) {
            def admin = session.getNode("/roles/system-administrator")
            if (admin.hasProperty("j:permissionNames")) {
                addValue(admin.getProperty("j:permissionNames"), "provisioningAccess")
            } else {
                admin.setProperty("j:permissionNames", ["provisioningAccess"] as String[])
            }
        } else {
            // If the node does not exists it means that it has been willingly removed, we don't move forward on the script
            log.info("Jahia role system-administrator does not exists anymore, add the provisioningAccess permission on wanted roles manually")
            return null
        }
        // Set permission on root-access
        log.info("Set permission on system-administrator/root-access external role")
        def rootAccess
        if (session.nodeExists("/roles/system-administrator/root-access")) {
            rootAccess = session.getNode("/roles/system-administrator/root-access")
            if (rootAccess.hasProperty("j:permissionNames")) {
                addValue(rootAccess.getProperty("j:permissionNames"), "provisioningAccess")
            } else {
                rootAccess.setProperty("j:permissionNames", ["provisioningAccess"] as String[])
            }
        } else {
            // Add the external role
            def admin = session.getNode("/roles/system-administrator")
            rootAccess = admin.addNode("root-access", "jnt:externalPermissions")
            rootAccess.setProperty("j:path", "/")
            rootAccess.setProperty("j:permissionNames", ["provisioningAccess"] as String[])
        }

        // Clean up rootNode-access
        if (session.nodeExists("/roles/system-administrator/rootNode-access")) {
            def rootNodeAccessRoleToRemove = session.getNode("/roles/system-administrator/rootNode-access")
            log.info("Node rootNode-access found")
            if (rootNodeAccessRoleToRemove.hasProperty("j:permissionNames")) {
                def rootNodePropertyPerms = rootNodeAccessRoleToRemove.getProperty("j:permissionNames").getValues()
                log.info("Merge existing permissions")
                def rootAccessPerms = rootAccess.getProperty("j:permissionNames")
                rootNodePropertyPerms.each { perm ->addValue(rootAccessPerms, perm.getString()) }
            }
            // Clean up root-access node role
            rootNodeAccessRoleToRemove.remove()
        }
        log.info("Save changes")
        session.save()
        return null
    }
})
log.info("End of provisioningAccess clean up")
