import org.jahia.settings.JahiaPropertiesUtils

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("contentHistoryPurge.cronExpression", "0 0 0 1 * ?",
            "# Control the scheduling of the job in charge of purging the content history table\n# Run at 00:00 on the 1st day of each month",
            null,
            "A new property was introduced with this version to control scheduling of the job in charge of purging the content history table.\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "contentHistoryPurge.cronExpression = 0 0 0 1 * ?");
    JahiaPropertiesUtils.addEntry("contentHistoryPurge.retentionInMonths", "13",
            "# How long the content history should be kept for (in months)\n",
            null,
            "A new property was introduced with this version to control how long the content history should be kept for (in months).\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "contentHistoryPurge.retentionInMonths = 13");
}

updateJahiaProperties();
