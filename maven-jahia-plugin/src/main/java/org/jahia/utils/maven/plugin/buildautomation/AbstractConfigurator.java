package org.jahia.utils.maven.plugin.buildautomation;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Feb 13, 2009
 * Time: 3:29:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractConfigurator {


    public static boolean forceDirs(File targetFile) {
        File targetFileFolder = targetFile.getParentFile();
        boolean result = true;
        // check if the destination folder exists and create it if needed...
        if (!targetFileFolder.exists()) {
            result = targetFileFolder.mkdirs();
        }
        return result;
    }
}
