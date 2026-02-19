import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties() {
    JahiaPropertiesUtils.replace("# Enables the document conversion service",
    "# Document converter is deprecated since Jahia 8.2.4.0 and will be removed in a future version.\n" +
            "# Enables the document conversion service");
    JahiaPropertiesUtils.replace("# Enables the document viewer service, which allows previewing documents",
            "# Document Viewer Service is deprecated since Jahia 8.2.4.0 and will be removed in a future version.\n" +
                    "# Enables the document viewer service, which allows previewing documents");
    JahiaPropertiesUtils.replace("# Enables the document thumbnails service. The service automatically creates",
            "# Document Viewer Service is deprecated since Jahia 8.2.4.0 and will be removed in a future version.\n" +
                    "# Enables the document thumbnails service. The service automatically creates");
    JahiaPropertiesUtils.replace("# Enables the video thumbnails service.",
            "# Document Viewer Service is deprecated since Jahia 8.2.4.0 and will be removed in a future version.\n" +
                    "# Enables the video thumbnails service.");
}

updateJahiaProperties();
