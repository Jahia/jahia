import org.jahia.settings.JahiaPropertiesUtils

//script from 7.3.4.0
// Update value of jahia.jquery.version if needed
// see https://jira.jahia.org/browse/QA-12350
JahiaPropertiesUtils.replace(
        "# JQuery module provides 1.12.4 and 3.3.1 , but other versions can be added in a separate module.",
        "# JQuery module provides 1.12.4 and 3.4.1 , but other versions can be added in a separate module."
)
JahiaPropertiesUtils.replace(
        "jahia.jquery.version = 3.3.1",
        "jahia.jquery.version = 3.4.1"
)

