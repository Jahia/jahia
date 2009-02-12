package org.jahia.utils.maven.plugin.deployers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Feb 12, 2009
 * Time: 4:33:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebsphereServerDeploymentImpl implements ServerDeploymentInterface {

    public static final String defaultSharedLibraryDirectory = "";
    public static final Map<String, String> sharedLibraryDirectory = new HashMap<String, String>();

    public boolean validateInstallationDirectory(String targetServerDirectory) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
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
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDeploymentDirPath(String name, String type) {
        return getDeploymentBaseDir() + "/" + name;
    }

    public String getDeploymentFilePath(String name, String type) {
        return getDeploymentBaseDir() + "/" + name;
    }
}
