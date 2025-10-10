import org.jahia.services.content.JCRTemplate
import org.jahia.settings.JahiaPropertiesUtils

// Add new props to existing jahia.properties file
JahiaPropertiesUtils.addEntry("jahia.user.passwordUpdate.currentPasswordRequired", "true",
        "# Enforces current password validation for user password changes.\n" +
                "# When enabled (recommended), password updates are only allowed through the secure\n" +
                "# JCRUserNode.setPassword(currentPassword, newPassword) method, which validates\n" +
                "# the current password before setting a new one.\n" +
                "#\n" +
                "# VALUES:\n" +
                "# - true:  (Default and recommended) Requires current password verification for all password changes.\n" +
                "#          Enhances security by preventing unauthorized password modifications.\n" +
                "# - false: (Legacy mode) Allows password changes without current password validation.\n" +
                "#          Use only if the secure method causes compatibility issues with existing implementations.\n" +
                "#\n" +
                "# SECURITY NOTE: Setting to false reduces security and is not recommended for production environments.\n" +
                "# Additional note: This setting does not affect new user creation, password can be set freely on newly created user node.\n" +
                "# Additional note: a new permission: setUsersPassword allow to bypass this setting, it should be granted only to administrators",
        ".*jahia\\.settings\\.memberDisplayLimit.*",
        "A new property was introduced with this version to introduce secured user password updates,\n" +
                "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                "jahia.user.passwordUpdate.currentPasswordRequired = true")

JahiaPropertiesUtils.addEntry("jahia.user.passwordUpdate.authorizationTimeoutMs", "10000",
        "# Specifies the duration (in milliseconds) for which password update authorization remains valid\n" +
                "# after successful password verification. This timeout controls the thread-local authorization window\n" +
                "# that allows password modifications following verifyPassword() calls.\n" +
                "#\n" +
                "# NOTE: This setting only applies when jahia.user.passwordUpdate.currentPasswordRequired is set to true.\n" +
                "# If jahia.user.passwordUpdate.currentPasswordRequired is false, this timeout has no effect as no authorization is required.\n" +
                "#\n" +
                "# Default: 10000 (10 seconds) - Balance between security and usability",
        ".*jahia\\.user\\.passwordUpdate\\.currentPasswordRequired.*",
        "A new property was introduced with this version to configure the duration of user password update authorization window,\n" +
                "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                "jahia.user.passwordUpdate.authorizationTimeoutMs = 10000")

// Create the setUsersPassword permission to allow password updates without current password verification (for admins)
JCRTemplate.instance.doExecuteWithSystemSession { session ->
    def node = session.getNode("/permissions")
    if (!node.hasNode("unsecure-permissions")) {
        def parent = node.addNode("unsecure-permissions", "jnt:permission")
        parent.addNode("setUsersPassword", "jnt:permission")
        session.save()
    }
    return null
}
