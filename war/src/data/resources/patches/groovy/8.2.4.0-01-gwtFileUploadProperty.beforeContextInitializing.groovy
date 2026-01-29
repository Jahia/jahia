import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("gwtFileUploadEnabled", "false",
            "# Enables or disables the legacy GWT file upload functionality and associated GWT UI actions.\n" +
            "# This API is deprecated and will be removed in a future version.\n" +
            "#\n" +
            "# When set to 'false' or kept unset:\n" +
            "#    - Import/Update menu actions will be disabled\n" +
            "#    - The GWTFileManagerUploadServlet POST endpoint will be unavailable\n" +
            "#\n" +
            "# RECOMMENDATION:\n" +
            "# Set to 'false', or keep unset, to disable this deprecated feature and migrate to modern file upload solutions.\n" +
            "# Keep as 'true' only if you still rely on legacy GWT-based file uploads.",
            ".*(?<!\\()jahiaSecuredFileUpload.*",
            "A new property was introduced with this version to disable the legacy GWT file upload functionality by default.\n" +
            "Please manually add the property gwtFileUploadEnabled into your jahia.properties file and set to your desired " +
            "setting if you need to change the default value\n");
}

updateJahiaProperties();
