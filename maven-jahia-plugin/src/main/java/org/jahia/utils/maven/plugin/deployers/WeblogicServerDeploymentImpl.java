package org.jahia.utils.maven.plugin.deployers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 10 févr. 2009
 * Time: 15:13:25
 * To change this template use File | Settings | File Templates.
 */
public class WeblogicServerDeploymentImpl implements ServerDeploymentInterface{
    public static final String defaultSharedLibraryDirectory = "lib";
    public static final Map<String, String> sharedLibraryDirectory = new HashMap<String, String>();
    /**
     * Returns true if the specified directory indeeed contains a valid installation of the application server
     *
     * @param targetServerDirectory
     * @return
     */
    public boolean validateInstallationDirectory(String targetServerDirectory) {
        return true;
    }

     private String getSharedLibraryDirectory(String serverVersion) {
        String result = sharedLibraryDirectory.get(serverVersion);
        if (result != null) return result;
        return defaultSharedLibraryDirectory;
    }

    public boolean deploySharedLibraries(String targetServerDirectory, String serverVersion, List<File> pathToLibraries) throws IOException {
        Iterator<File> libraryPathIterator = pathToLibraries.iterator();
        File targetDirectory = new File(targetServerDirectory, getSharedLibraryDirectory(serverVersion));
        while (libraryPathIterator.hasNext()) {
            File currentLibraryPath = libraryPathIterator.next();
            FileUtils.copyFileToDirectory(currentLibraryPath, targetDirectory);
        }
        return true;
    }

    public boolean undeploySharedLibraries(String targetServerDirectory, String serverVersion, List<File> pathToLibraries) throws IOException {
        Iterator<File> libraryPathIterator = pathToLibraries.iterator();
        File targetDirectory = new File(targetServerDirectory, getSharedLibraryDirectory(serverVersion));
        while (libraryPathIterator.hasNext()) {
            File currentLibraryPath = libraryPathIterator.next();
            File targetFile = new File(targetDirectory, currentLibraryPath.getName());
            targetFile.delete();
        }
        return true;
    }

    public String getDeploymentBaseDir() {
        return "";
    }

    public String getDeploymentDirPath(String name, String type) {
        return getDeploymentBaseDir() + "/" + name;
    }

    public String getDeploymentFilePath(String name, String type) {
        return getDeploymentBaseDir() + "/" + name + "." + type;
    }
}
