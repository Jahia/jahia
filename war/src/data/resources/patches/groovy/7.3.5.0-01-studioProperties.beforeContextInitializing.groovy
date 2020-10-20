import org.jahia.settings.JahiaPropertiesUtils

// QA-11872
JahiaPropertiesUtils.addEntry("studioMaxDisplayableFileSize",
        "1048576",
        "\n######################################################################\n" +
                "### Studio ###########################################################\n" +
                "######################################################################\n" +
                "# Maximum size of file in bytes that can be displayed in the studio editor\n",
        null,
        "A new property was introduced with this version to handle the display of big file in the Studio,\n" +
                "by default file that exceed 1048576 bytes are not displayed anymore.\n" +
                "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                "studioMaxDisplayableFileSize = 1048576")
