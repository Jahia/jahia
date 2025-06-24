import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("jahiaFileUploadCountMax", "50",
            "# Limit the number of request parts to be processed, prevents DDOS attack, see https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2023-24998",
            ".*jahiaFileUploadMaxSize.*",
            "A new property was introduced with this version to limit the number of request parts to be processed,\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "jahiaFileUploadCountMax = 50");
}

updateJahiaProperties();
