import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("auth.valve.basicAccess.enabled", "true",
            "# HttpBasicAuthValveImpl:\n",
            ".*auth\\.token\\.enabled.*",
            "A new property was introduced with this version to enable the basic access authentication valve (HttpBasicAuthValveImpl).\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "auth.valve.basicAccess.enabled = true");
    JahiaPropertiesUtils.addEntry("auth.valve.loginEngine.enabled", "true",
            "# LoginEngineAuthValveImpl:\n",
            ".*auth\\.valve\\.basicAccess\\.enabled.*",
            "A new property was introduced with this version to enable the login engine valve " +
                    "(LoginEngineAuthValveImpl).\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "auth.valve.loginEngine.enabled = true");
    JahiaPropertiesUtils.addEntry("auth.valve.session.enabled", "true",
            "# LoginEngineAuthValveImpl:\n",
            ".*auth\\.valve\\.loginEngine\\.enabled.*",
            "A new property was introduced with this version to enable the session valve " +
                    "(SessionAuthValveImpl).\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "auth.valve.session.enabled = true");
}

updateJahiaProperties();
