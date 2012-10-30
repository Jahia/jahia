/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

//
//
//  JahiaTemplatePackageHandler
//
//  NK      13.01.2001
//
//

package org.jahia.services.templates;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This class is responsible for loading data from a Template Jar File
 *
 * @author Khue ng
 */
final class JahiaTemplatesPackageHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(JahiaTemplatesPackageHandler.class);

    /**
     * Returns the Generated JahiaTemplatesPackage Object
     *
     * @return (JahiaTemplatesPackage) the package object
     */
    public static JahiaTemplatesPackage build(File packageSource) {
        return postProcess(read(packageSource), packageSource);
    }

    private static JahiaTemplatesPackage postProcess(
            JahiaTemplatesPackage templatePackage, File file) {
        
        long timer = System.currentTimeMillis();
        if (templatePackage == null) {
            templatePackage = new JahiaTemplatesPackage();
        }

        templatePackage.setFilePath(file.getPath());

        if (StringUtils.isEmpty(templatePackage.getName())) {
            templatePackage.setRootFolder(file.getParentFile().getName());
        }
        if (StringUtils.isEmpty(templatePackage.getRootFolder())) {
            templatePackage.setRootFolder(file.getParentFile().getName());
        }
        if (StringUtils.isEmpty(templatePackage.getVersion().toString())) {
            templatePackage.setVersion(new ModuleVersion("SNAPSHOT"));
        }

        if (templatePackage.getDefinitionsFiles().isEmpty()) {
            // check if there is a definitions file
            if (new File(file, "definitions.cnd").exists()) {
                templatePackage.setDefinitionsFile("definitions.cnd");
                warnMetaInf("definitions.cnd", templatePackage.getRootFolder());
            }
            if (new File(file, "META-INF/definitions.cnd").exists()) {
                templatePackage.setDefinitionsFile("META-INF/definitions.cnd");
            }
        }
        if (templatePackage.getRulesDescriptorFiles().isEmpty()) {
            // check if there is a rules file
            if (new File(file, "rules.dsl").exists()) {
                templatePackage.setRulesDescriptorFile("rules.dsl");
                warnMetaInf("rules.dsl", templatePackage.getRootFolder());
            }
            if (new File(file, "META-INF/rules.dsl").exists()) {
                templatePackage.setRulesDescriptorFile("META-INF/rules.dsl");
            }
        }

        // check if there is a rules file
        if (new File(file, "rules.drl").exists()) {
            templatePackage.setRulesFile("rules.drl");
            warnMetaInf("rules.drl", templatePackage.getRootFolder());
        }
        if (new File(file, "META-INF/rules.drl").exists()) {
            templatePackage.setRulesFile("META-INF/rules.drl");
        }
        // check if there is a rules file
        if (new File(file, "live-rules.drl").exists()) {
            templatePackage.setRulesFile("live-rules.drl");
            warnMetaInf("live-rules.drl", templatePackage.getRootFolder());
        }
        if (new File(file, "META-INF/live-rules.drl").exists()) {
            templatePackage.setRulesFile("META-INF/live-rules.drl");
        }
        // check if there is a rules file
        if (new File(file, "default-rules.drl").exists()) {
            templatePackage.setRulesFile("default-rules.drl");
            warnMetaInf("default-rules.drl", templatePackage.getRootFolder());
        }
        if (new File(file, "META-INF/default-rules.drl").exists()) {
            templatePackage.setRulesFile("META-INF/default-rules.drl");
        }

        if (templatePackage.getResourceBundleName() == null) {
            // check if there is a resource bundle file in the resources folder
            String rbName = Patterns.SPACE.matcher(templatePackage.getName()).replaceAll("");
            if (new File(file, "/resources/" + rbName + ".properties").exists()) {
                templatePackage.setResourceBundleName("resources." + rbName);
            } else {
                rbName = Patterns.SPACE.matcher(templatePackage.getName()).replaceAll("_");
                if (new File(file, "/resources/" + rbName + ".properties").exists()) {
                    templatePackage.setResourceBundleName("resources." + rbName);
                }
            }
        }

        if (templatePackage.getInitialImports().isEmpty()) {
            List<File> files = Arrays.asList(file.listFiles((FilenameFilter) new WildcardFileFilter(new String[]{
                    "import*.xml", "import*.zip"}, IOCase.INSENSITIVE)));
            Comparator<File> c = new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return StringUtils.substringBeforeLast(o1.getName(), ".").compareTo(StringUtils.substringBeforeLast(o2.getName(), "."));
                }
            };
            Collections.sort(files, c);
            for (File importFile : files) {
                templatePackage.addInitialImport(importFile.getName());
                warnMetaInf("importFile.getName()", templatePackage.getRootFolder());
            }
            File metaInf = new File(file, "META-INF");
            if (metaInf.exists()) {
                files = Arrays.asList(metaInf.listFiles((FilenameFilter) new WildcardFileFilter(new String[]{
                        "import*.xml", "import*.zip"}, IOCase.INSENSITIVE)));
                Collections.sort(files, c);
                for (File importFile : files) {
                    templatePackage.addInitialImport("META-INF/" + importFile.getName());
                }
            }
        }

        logger.debug("Execution took {} ms", (System.currentTimeMillis() - timer));
        
        return templatePackage;
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
                InputStream manifestStream = new BufferedInputStream(new FileInputStream(manifestFile), 1024);
                Manifest manifest = new Manifest(manifestStream);
                IOUtils.closeQuietly(manifestStream);
                String packageName = (String) manifest.getMainAttributes().get(new Attributes.Name("package-name"));
                String rootFolder = (String) manifest.getMainAttributes().get(new Attributes.Name("root-folder"));
                String moduleType = (String) manifest.getMainAttributes().get(new Attributes.Name("module-type"));
                String implementationVersionStr = (String) manifest.getMainAttributes().get(new Attributes.Name("Implementation-Version"));

                String depends = (String) manifest.getMainAttributes().get(new Attributes.Name("depends"));
                if (depends != null && !StringUtils.isEmpty(depends.trim())) {
                    String[] dependencies = Patterns.COMMA.split(depends);
                    for (String dependency : dependencies) {
                        templatePackage.setDepends(dependency.trim());
                    }
                }

                String definitions = (String) manifest.getMainAttributes().get(new Attributes.Name("definitions"));
                if (definitions != null) {
                    String[] defs = Patterns.COMMA.split(definitions);
                    for (String defFile : defs) {
                        templatePackage.getDefinitionsFiles().add(defFile.trim());
                    }
                }

                String imports = (String) manifest.getMainAttributes().get(new Attributes.Name("initial-imports"));
                if (imports != null) {
                    String[] importFiles = Patterns.COMMA.split(imports);
                    for (String imp : importFiles) {
                        templatePackage.addInitialImport(imp.trim());
                    }
                }
                
                String resourceBundle = (String) manifest.getMainAttributes().get(new Attributes.Name("resource-bundle"));
                if (StringUtils.isNotBlank(resourceBundle)) {
                    templatePackage.setResourceBundleName(resourceBundle.trim());
                }
                String autoDeployOnSite = (String) manifest.getMainAttributes().get(new Attributes.Name("deploy-on-site"));
                if(autoDeployOnSite != null && autoDeployOnSite.matches("systemsite|all")){
                    templatePackage.setAutoDeployOnSite(autoDeployOnSite);
                }
                templatePackage.setName(packageName);
                templatePackage.setRootFolder(rootFolder);
                templatePackage.setModuleType(moduleType);
                templatePackage.setVersion(new ModuleVersion(implementationVersionStr));

                String provider = (String) manifest.getMainAttributes().get(new Attributes.Name("Implementation-Vendor"));
                templatePackage.setProvider(StringUtils.defaultIfBlank(provider, "Jahia Solutions Group SA"));
            }
        } catch (IOException ioe) {
            logger
                    .warn(
                            "Failed extracting module package data from META-INF/MANIFEST.MF file for package "
                                    + file, ioe);
        }

        return templatePackage;
    }

    private static void warnMetaInf(String resourtce, String module) {
        logger.warn("{} file found in the root folder of the module '{}'."
                + " Consider moving it under META-INF/"
                + " folder to prevent security vulnerabilities.", resourtce, module);
    }

    private JahiaTemplatesPackageHandler() {
        super();
    }

}