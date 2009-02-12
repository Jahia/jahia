/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin.deployers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 26 d�c. 2007
 * Time: 14:18:34
 * To change this template use File | Settings | File Templates.
 */
public class Tomcat5ServerDeploymentImpl implements ServerDeploymentInterface {

    public static final String defaultSharedLibraryDirectory = "lib";
    public static final Map<String, String> sharedLibraryDirectory = new HashMap<String, String>() { {
           put("5.5", "shared/lib");
           put("6", "lib"); }};

    public static final String defaultEndorsedLibraryDirectory = "endorsed";
    public static final Map<String, String> endorsedLibraryDirectory = new HashMap<String, String>() { {
           put("5.5", "common/endorsed");
           put("6", "endorsed"); }};


    private String getEndorsedLibraryDirectory(String serverVersion) {
        String result = endorsedLibraryDirectory.get(serverVersion);
        if (result != null) return result;
        return defaultEndorsedLibraryDirectory;
    }

    private String getSharedLibraryDirectory(String serverVersion) {
        String result = sharedLibraryDirectory.get(serverVersion);
        if (result != null) return result;
        return defaultSharedLibraryDirectory;
    }

    public boolean validateInstallationDirectory(String targetServerDirectory) {
        File serverConfig = new File(targetServerDirectory, "conf/server.xml");
        File catalinaProps = new File(targetServerDirectory, "conf/catalina.properties");
        return serverConfig.exists() && catalinaProps.exists();
    }

    public boolean deploySharedLibraries(String targetServerDirectory,
                                         String serverVersion,
                                         List<File> pathToLibraries) throws IOException {
        Iterator<File> libraryPathIterator = pathToLibraries.iterator();
        File targetDirectory = new File(targetServerDirectory, getSharedLibraryDirectory(serverVersion));
        File targetEndorsedDirectory = new File(targetServerDirectory, getEndorsedLibraryDirectory(serverVersion));
        while (libraryPathIterator.hasNext()) {
            File currentLibraryPath = libraryPathIterator.next();
            if (currentLibraryPath.getName().contains("jaxb-api")) {
                FileUtils.copyFileToDirectory(currentLibraryPath, targetEndorsedDirectory);   
            } else {
                FileUtils.copyFileToDirectory(currentLibraryPath, targetDirectory);
            }
        }
        return true;
    }

    public boolean undeploySharedLibraries(String targetServerDirectory,
                                           String serverVersion,
                                           List<File> pathToLibraries) throws IOException {
        Iterator<File> libraryPathIterator = pathToLibraries.iterator();
        File targetDirectory = new File(targetServerDirectory, getSharedLibraryDirectory(serverVersion));
        File targetEndorsedDirectory = new File(targetServerDirectory, getEndorsedLibraryDirectory(serverVersion));
        while (libraryPathIterator.hasNext()) {
            File currentLibraryPath = libraryPathIterator.next();
            if (currentLibraryPath.getName().contains("jaxb-api")) {
                File targetFile = new File(targetEndorsedDirectory, currentLibraryPath.getName());
                targetFile.delete();
            } else {
                File targetFile = new File(targetDirectory, currentLibraryPath.getName());
                targetFile.delete();
            }
        }
        return true;
    }
    
    public String getDeploymentBaseDir() {
        return "webapps";
    }

    public String getDeploymentDirPath(String name, String type) {
        return getDeploymentBaseDir() + "/" + name;
    }

    public String getDeploymentFilePath(String name, String type) {
        return getDeploymentBaseDir() + "/" + name + "." + type;
    }
}
