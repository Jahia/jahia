import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("auth.token.enabled", "false",
            null,
            ".*auth\\.container\\.enabled.*",
            "A new property was introduced with this version to disable a deprecated authentication valve: TokenAuthValve,\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "auth.token.enabled = false");
}

updateJahiaProperties();
