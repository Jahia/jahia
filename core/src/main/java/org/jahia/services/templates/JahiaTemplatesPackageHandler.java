/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//  JahiaTemplatePackageHandler
//
//  NK      13.01.2001
//
//

package org.jahia.services.templates;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This class is responsible for loading data from a Template Jar File
 *
 * @author Khue ng
 */
final class JahiaTemplatesPackageHandler {

    private static final Logger logger = Logger
            .getLogger(JahiaTemplatesPackageHandler.class);

    private JahiaTemplatesPackageHandler() {
        super();
    }

    /**
     * Extract data from the MANIFEST.MF file and builds the
     * JahiaTemplatesPackage object
     *
     * @param file the package file to read
     */
    private static JahiaTemplatesPackage read(File file) {

        JahiaTemplatesPackage templatePackage = new JahiaTemplatesPackage();
        // extract data from the META-INF/MANIFEST.MF file
        try {
            File manifestFile = new File(file, "META-INF/MANIFEST.MF");
            if (manifestFile.exists()) {
                FileInputStream manifestStream = new FileInputStream(
                        manifestFile);
                Manifest manifest = new Manifest(manifestStream);
                IOUtils.closeQuietly(manifestStream);
                String packageName = (String) manifest.getMainAttributes().get(new Attributes.Name("package-name"));
                String rootFolder = (String) manifest.getMainAttributes().get(new Attributes.Name("root-folder"));
                if (packageName == null) {
                    packageName = file.getName();
                }
                if (rootFolder == null) {
                    rootFolder = file.getName();
                }

                String depends = (String) manifest.getMainAttributes().get(new Attributes.Name("depends"));
                if (depends != null) {
                    String[] dependencies = depends.split(",");
                    for (int i = 0; i < dependencies.length; i++) {
                        String dependency = dependencies[i].trim();
                        templatePackage.setDepends(dependency);
                    }
                }

                String imports = (String) manifest.getMainAttributes().get(new Attributes.Name("initial-imports"));
                if (imports != null) {
                    String[] importFiles = imports.split(",");
                    for (String imp : importFiles) {
                        templatePackage.addInitialImport(imp.trim());
                    }
                }

                templatePackage.setName(packageName);
                templatePackage.setRootFolder(rootFolder);
            }
        } catch (IOException ioe) {
            logger
                    .warn(
                            "Failed extracting template package data from META-INF/MANIFEST.MF file for package "
                                    + file, ioe);
        }

        return templatePackage;
    }

    private static JahiaTemplatesPackage postProcess(
            JahiaTemplatesPackage templatePackage, File file) {
        if (templatePackage == null) {
            templatePackage = new JahiaTemplatesPackage();
        }

        templatePackage.setFilePath(file.getPath());

        if (StringUtils.isEmpty(templatePackage.getName())) {
            templatePackage.setName(FilenameUtils.getBaseName(file.getPath()));
        }
        if (StringUtils.isEmpty(templatePackage.getRootFolder())) {
            templatePackage.setRootFolder((FilenameUtils.getBaseName(file
                    .getPath()).replace('-', '_')).toLowerCase());
        }
        if (templatePackage.getDefinitionsFiles().isEmpty()) {
            // check if there is a definitions file
            if (new File(file, "definitions.cnd").exists()) {
                templatePackage.setDefinitionsFile("definitions.cnd");
            }
            // check if there is a definitions grouping file
            if (new File(file, "definitions.grp").exists()) {
                templatePackage.setDefinitionsFile("definitions.grp");
            }
        }
        if (templatePackage.getRulesFiles().isEmpty()) {
            // check if there is a rules file
            if (new File(file, "rules.drl").exists()) {
                templatePackage.setRulesFile("rules.drl");
            }
        }
        if (templatePackage.getResourceBundleName() == null) {
            // check if there is a resource bundle file in the resources folder
            String rbName = templatePackage.getName().replace(' ', '_');
            if (new File(file, "resources/" + rbName + ".properties").exists()) {
                templatePackage.setResourceBundleName("resources." + rbName);
            } else {
                rbName = templatePackage.getName().replace(" ", "");
                if (new File(file, "resources/" + rbName + ".properties")
                        .exists()) {
                    templatePackage
                            .setResourceBundleName("resources." + rbName);
                }
            }
        }

        if (templatePackage.getInitialImports().isEmpty()) {
            File[] files = file.listFiles((FilenameFilter) new WildcardFileFilter(new String[]{
                    "import.xml", "import.zip", "import-*.xml", "import-*.zip"}, IOCase.INSENSITIVE));
            Arrays.sort(files);
            for (File importFile : files) {
                templatePackage.addInitialImport(importFile.getName());
            }
        }

        return templatePackage;
    }

    /**
     * Returns the Generated JahiaTemplatesPackage Object
     *
     * @return (JahiaTemplatesPackage) the package object
     */
    public static JahiaTemplatesPackage build(File packageSource) {
        return postProcess(read(packageSource), packageSource);
    }

}