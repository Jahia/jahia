import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("jahia.jcr.nodesCachePerSessionMaxSize", "100",
            "# Specifies the maximum number of entries allowed in the JCRNodeWrapper instances cache.\n" +
                    "# These caches are designed to optimize direct access to JCR nodes by storing recently accessed entries.\n" +
                    "# The caches are implemented as LRU (Least Recently Used) caches, retaining the specified number of entries\n" +
                    "# in memory and removing the least recently used entries when the limit is exceeded.\n" +
                    "# Two caches are created for each JCR session: one cache per path and one cache per identifier.\n" +
                    "# JCR sessions are typically short-lived, except in cases of long-running operations such as exports,\n" +
                    "# publications, and copies.\n" +
                    "# If no value is specified, the default value is 100 entries.\n" +
                    "# Setting this property to zero or a negative value will disable the limit.",
            ".*accessManagerPathPermissionCacheMaxSize.*",
            "A new property was introduced with this version to configure the maximum JCR node cached per session\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "jahia.jcr.nodesCachePerSessionMaxSize = 100");
}

updateJahiaProperties();
