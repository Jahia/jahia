import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("jahiaSecuredFileUpload", "true",
            "# Controls whether file uploads in multipart requests are subject to security validation.\n" +
                    "#\n" +
                    "# When processing multipart requests (typically file upload forms) targeting Actions, Webflow,\n" +
                    "# views, and other Jahia rendering servlet subsystems, uploaded files are temporarily stored\n" +
                    "# in the file system (tmpContentDiskPath) before being processed by the target subsystem.\n" +
                    "#\n" +
                    "# VALUES:\n" +
                    "# - true:  (Default and recommended) Enables security validation for file uploads.\n" +
                    "#          Files in multipart requests will be rejected if:\n" +
                    "#          - User lacks required privileges for the target subsystem\n" +
                    "#          - Privileges cannot be verified (fail-secure approach)\n" +
                    "#          - Target JCR path exists but user lacks write permissions\n" +
                    "#\n" +
                    "#          This prevents unauthorized files from being written to the temporary folder,\n" +
                    "#          reducing disk usage and security risks.\n" +
                    "#\n" +
                    "# - false: (For backward compatibility) Disables security validation.\n" +
                    "#          All files in multipart requests are processed and stored temporarily,\n" +
                    "#          regardless of user permissions.\n" +
                    "#\n" +
                    "# LIMITATIONS WHEN ENABLED (jahiaSecuredFileUpload=true):\n" +
                    "#\n" +
                    "# 1. Form submissions using 'jcrTargetDirectory' field:\n" +
                    "#    - Will be rejected as the target directory cannot be pre-validated\n" +
                    "#    - Alternative: Use ajax file upload with targeted path in the URL, it's secured with permission check.\n" +
                    "#\n" +
                    "# 2. Custom Actions implementation requiring file uploads:\n" +
                    "#    - Only work if the action is:\n" +
                    "#      - Configured with requiredAuthenticatedUser=true, OR\n" +
                    "#      - Protected with specific requiredPermission settings\n" +
                    "#    - Anonymous file upload actions will be blocked\n" +
                    "#    - Security checks are enforced despite tokenization: <template:tokenizedForm>\n" +
                    "#\n" +
                    "# 4. Webflow components requiring file uploads:\n" +
                    "#    - Only functional in:\n" +
                    "#      - Edit mode\n" +
                    "#      - Admin areas: /settings (server admin) and /sites/{siteKey} (site admin)\n" +
                    "#    - File uploads in live mode webflows will be blocked\n" +
                    "#\n" +
                    "# RECOMMENDATION:\n" +
                    "# Set to 'true' in production environments for enhanced security.\n" +
                    "# Set to 'false' only if you require legacy code cases and unrestricted file upload behavior.",
            ".*jahiaFileUploadCountMax.*",
            "A new property was introduced with this version to secure file upload handling on request targeting Jahia render servlet,\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "jahiaSecuredFileUpload = true");
}

updateJahiaProperties();
