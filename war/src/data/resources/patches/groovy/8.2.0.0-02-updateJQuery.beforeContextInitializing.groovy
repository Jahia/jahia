import org.jahia.settings.JahiaPropertiesUtils

// Script from 8.2.0.0
// Update value of jahia.jquery.version if needed
// See https://jira.jahia.org/browse/QA-12350
JahiaPropertiesUtils.replace(
        "# The JQuery module provides versions 1.12.4, 3.4.1 and 3.6.0. Other versions can be added in a separate module.",
        "# The JQuery module provides versions 3.7.1. Other versions can be added in a separate module."
)
JahiaPropertiesUtils.replace(
        "jahia.jquery.version = 3.4.1",
        "jahia.jquery.version = 3.7.1"
)

