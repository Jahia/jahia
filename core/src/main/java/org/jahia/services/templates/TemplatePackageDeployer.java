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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.templates;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.jahia.services.deamons.filewatcher.FileListSync;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.zip.JahiaArchiveFileHandler;
import org.jahia.engines.calendar.CalendarHandler;

/**
 * Template package deployer service.
 *
 * @author Sergiy Shyrkov
 */
class TemplatePackageDeployer {

    private static Logger logger = Logger
            .getLogger(TemplatePackageDeployer.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            CalendarHandler.DEFAULT_DATE_FORMAT);

    /**
     * Meta-Inf folder *
     */
    private static final String META_INF_FOLDER = "Meta-Inf";

    /**
     * The templates classes Root Path *
     */
    private static final String TEMPLATES_CLASSES_ROOT_FOLDER = "jahiatemplates";

    private SettingsBean settingsBean;

    private TemplatePackageRegistry templatePackageRegistry;

    /**
     * Deploys a template package.
     *
     * @param packageHandler the template package handler object
     * @throws JahiaTemplateServiceException
     */
    private void deployPackage(JahiaTemplatesPackageHandler packageHandler)
            throws JahiaTemplateServiceException {

        JahiaTemplatesPackage tmplPack = packageHandler.getPackage();

        logger.info("Start deploying new template package '"
                + tmplPack.getName() + "'");

        if (templatesFolderAlreadyExists(tmplPack.getRootFolder())) {
            throw new JahiaTemplateServiceException(
                    "Unable to deploy template package '" + tmplPack.getName()
                            + "'. Folder '" + tmplPack.getRootFolder()
                            + "' already exists");
        }

        File tmplRootFolder = new File(
                settingsBean.getJahiaTemplatesDiskPath(), tmplPack
                        .getRootFolder());

        tmplRootFolder.mkdirs();
        try {

            packageHandler.unzip(tmplRootFolder.getAbsolutePath());

            // Remove the Meta-Inf folder first
            removeMetaInfFolder(tmplRootFolder.getAbsolutePath());

            // extract the classes file
            if (tmplPack.hasClasses()) {
                File tmpFile = new File(tmplRootFolder, tmplPack
                        .getClassesFile());
                if (tmpFile.exists() && tmpFile.isFile()) {
                    JahiaArchiveFileHandler arch = new JahiaArchiveFileHandler(
                            tmpFile.getAbsolutePath());
                    if (tmplPack.getClassesRoot() == null) {
                        arch.unzip(settingsBean.getClassDiskPath()
                                + File.separator
                                + TEMPLATES_CLASSES_ROOT_FOLDER);
                    } else {
                        arch.unzip(settingsBean.getClassDiskPath()
                                + File.separator + tmplPack.getClassesRoot());
                    }
                }
            }
            if (tmplPack.isWar()) {
                FileUtils.copyDirectory(new File(tmplRootFolder, "WEB-INF/classes"),
                        new File(settingsBean.getClassDiskPath()));
                FileUtils.deleteDirectory(new File(tmplRootFolder, "WEB-INF"));
            }
        } catch (JahiaException je) {
            logger.error(je);
            throw new JahiaTemplateServiceException(je.getMessage(), je);

        } catch (IOException ioe) {
            logger.error(ioe);
            throw new JahiaTemplateServiceException(
                    "Failed creating JahiaArchiveFileHandler on classes file",
                    ioe);
        }

        // overwrite template deployment descriptor
        TemplateDeploymentDescriptorHelper.serialize(
                tmplPack, tmplRootFolder);
        FileListSync.getInstance().trigger();

        logger.info("Package '" + tmplPack.getName()
                + "' successfully deployed");
    }

    /**
     * Deploys the template set from the specified JAR file that is located in
     * the shared templates folder. Returns the name of the deployed package.
     *
     * @param templateJar     the file of the template package JAR file
     * @param renameIfExists  rename he template package set and the root folder, in case of
     *                        conflicts
     * @param suggestedPrefix suggested prefix in case of renaming
     * @return the name of the deployed package
     * @throws JahiaException
     */
    public String deployTemplatePackage(File templateJar,
                                        boolean renameIfExists, String suggestedPrefix)
            throws JahiaException {
        JahiaTemplatesPackage pkg = null;
        try {
            JahiaTemplatesPackageHandler packageHandler = JahiaTemplatesPackageHandler.getPackageHandler(templateJar);
            pkg = packageHandler.getPackage();
            if (pkg != null) {
                if (isValidPackage(pkg)) {
                    deployTemplatePackage(packageHandler, renameIfExists, suggestedPrefix);
                }
            } else {
                throw new JahiaTemplateServiceException(
                        "Unable to read template package from the file: "
                                + templateJar.getAbsolutePath());
            }
        } catch (IllegalArgumentException ex) {
            throw new JahiaTemplateServiceException(
                    "Unable to read the templates deployment descriptor from "
                            + templateJar.getPath(), ex);
        }

        return pkg != null ? pkg.getName() : null;
    }

    private void deployTemplatePackage(JahiaTemplatesPackageHandler packageHandler, boolean renameIfExists, String suggestedPrefix) throws JahiaTemplateServiceException {
        JahiaTemplatesPackage pkg = packageHandler.getPackage();
        logger.debug("Template package found: " + pkg.getName());
        if (!templatePackageRegistry.contains(pkg.getName())
                && !templatesFolderAlreadyExists(pkg.getRootFolder())) {
            deployPackage(packageHandler);
            templatePackageRegistry.register(pkg);
        } else {
            if (renameIfExists) {
                String pkgName = pkg.getName();
                String rootFolder = pkg.getRootFolder();
                if (StringUtils.isNotEmpty(suggestedPrefix)) {
                    pkgName = suggestedPrefix + "_" + pkg.getName();
                    rootFolder = suggestedPrefix + "_"
                            + pkg.getRootFolder();
                }

                if (templatePackageRegistry.contains(pkgName)
                        || templatesFolderAlreadyExists(rootFolder)) {
                    String postfix = DATE_FORMAT.format(new Date());
                    String pkgNameBase = pkgName + "_" + postfix;
                    String rootFolderBase = rootFolder + "_" + postfix;
                    pkgName = pkgNameBase;
                    rootFolder = rootFolderBase;
                    int postfixCounter = 0;
                    while (templatePackageRegistry.contains(pkgName)
                            || templatesFolderAlreadyExists(rootFolder)) {
                        ++postfixCounter;
                        pkgName = pkgNameBase + "_" + postfixCounter;
                        rootFolder = rootFolderBase + "_"
                                + postfixCounter;
                    }
                }

                // change package name and folder
                pkg.setName(pkgName);
                pkg.setRootFolder(rootFolder);

                // deploy and register
                deployPackage(packageHandler);
                templatePackageRegistry.register(pkg);
            } else {
                logger.debug("Template package '" + pkg.getName()
                        + "' already exists. Skipping.");
            }
        }
    }

    private boolean isValidPackage(JahiaTemplatesPackage pkg) {
        if (StringUtils.isEmpty(pkg.getName())) {
            logger.warn("Template package name '" + pkg.getName()
                    + "' is not valid. Setting it to 'templates'.");
            pkg.setName("templates");
        }
        if (StringUtils.isEmpty(pkg.getRootFolder())) {
            String folderName = pkg.getName().replace(' ', '_').toLowerCase();
            logger.warn("Template package root folder '" + pkg.getRootFolder()
                    + "' is not valid. Setting it to '" + folderName + "'.");
            pkg.setRootFolder(folderName);
        }
        return true;
    }

    /**
     * Search and remove the META-INF folder
     *
     * @param parentPath the path, where to search for META-INF folder
     * @return (boolean) true if successful
     */
    private void removeMetaInfFolder(String parentPath) {
        if (parentPath == null) {
            return;
        }

        File f = new File(parentPath);
        File[] files = f.listFiles((FileFilter) new NameFileFilter(
                META_INF_FOLDER, IOCase.INSENSITIVE));
        for (int i = 0; i < files.length; i++) {
            try {
                JahiaTools.deleteFile(files[i], false);
            } catch (Exception ex) {
                logger.error("Unable to delete META-INF directory "
                        + files[i].getAbsolutePath(), ex);
            }
        }
    }

    public void scanDeployedTemplatePackages() {
        logger.info("Scanning templates directory...");
        File templatesRoot = new File(settingsBean.getJahiaTemplatesDiskPath());
        if (templatesRoot.isDirectory()) {
            String[] dirs = templatesRoot.list(DirectoryFileFilter.DIRECTORY);

            List<JahiaTemplatesPackageHandler> remaining = new ArrayList<JahiaTemplatesPackageHandler>();

            for (int i = 0; i < dirs.length; i++) {
                File templateDir = new File(settingsBean.getJahiaTemplatesDiskPath(), dirs[i]);

                logger.debug("Checking directory: " + dirs[i]);
                if (JahiaTemplatesPackageHandler.isValidTemplatesDirectory(templateDir.getAbsolutePath())) {
                    try {
                        logger.debug("Reading the templates set under "
                                + dirs[i]);
                        JahiaTemplatesPackageHandler packageHandler = new JahiaTemplatesPackageHandler(templateDir.getAbsolutePath());
                        JahiaTemplatesPackage pkg = packageHandler.getPackage();
                        if (pkg != null) {
                            logger.debug("Template package found: " + pkg.getName());
                            if (isValidPackage(pkg)) {
                                remaining.add(packageHandler);
                            }
                        } else {
                            logger.warn("Unable to read template package from the directory " + templateDir);
                        }
                    } catch (JahiaException ex) {
                        logger.warn("Unable to read the templates deployment descriptor under " + templateDir, ex);
                    }
                }
            }

            ListOrderedMap toDeploy = getOrderedPackages(remaining);
            for (Iterator iterator = toDeploy.values().iterator(); iterator.hasNext();) {
                JahiaTemplatesPackageHandler handler = (JahiaTemplatesPackageHandler) iterator.next();
                templatePackageRegistry.register(handler.getPackage());
            }
        }
        logger.info("...finished scanning templates directory. Found "
                + templatePackageRegistry.getAvailablePackagesCount()
                + " template packages.");
    }

    public void scanSharedTemplatePackages() {
        logger.info("Scanning shared templates directory...");

        File sharedTemplates = new File(settingsBean
                .getJahiaSharedTemplatesDiskPath());
        // create shared templates folder if it does not exist
        if (!sharedTemplates.exists()) {
            sharedTemplates.mkdirs();
        }

        if (sharedTemplates.exists() && sharedTemplates.isDirectory()) {
            String[] jarFiles = sharedTemplates.list(new SuffixFileFilter(new String[]{".jar", ".war"}));
            // iterate over found JAR files and deploy them
            List<JahiaTemplatesPackageHandler> remaining = new ArrayList<JahiaTemplatesPackageHandler>();

            for (int i = 0; i < jarFiles.length; i++) {
                File templateJar = new File(settingsBean.getJahiaSharedTemplatesDiskPath(), jarFiles[i]);
                try {
                    JahiaTemplatesPackageHandler packageHandler = JahiaTemplatesPackageHandler.getPackageHandler(templateJar);
                    JahiaTemplatesPackage pkg = packageHandler.getPackage();
                    if (pkg != null) {
                        if (isValidPackage(pkg)) {
                            remaining.add(packageHandler);
                        }
                    } else {
                        logger.error("Unable to read the templates package from the file: " + templateJar.getAbsolutePath());
                    }
                } catch (IllegalArgumentException ex) {
                    logger.error("Unable to read the templates deployment descriptor from " + templateJar.getPath(), ex);
                } catch (JahiaException ex) {
                    logger.error("Unable to read the templates deployment descriptor from " + templateJar.getPath(), ex);
                }
            }

            ListOrderedMap toDeploy =  getOrderedPackages(remaining);
            for (Iterator iterator = toDeploy.values().iterator(); iterator.hasNext();) {
                JahiaTemplatesPackageHandler handler = (JahiaTemplatesPackageHandler) iterator.next();
                try {
                    deployTemplatePackage(handler, false, null);
                } catch (JahiaException e) {
                    logger.error("Error deploying package from JAR file '"
                            + handler.getPackage().getName() + "'. Skipping it.", e);
                }
            }
        }

        logger.info("...finished scanning shared templates directory.");
    }

    private ListOrderedMap getOrderedPackages(List<JahiaTemplatesPackageHandler> remaining) {
        ListOrderedMap toDeploy = new ListOrderedMap();
        while (!remaining.isEmpty()) {
            List<JahiaTemplatesPackageHandler> newRemaining = new ArrayList<JahiaTemplatesPackageHandler>();
            for (Iterator iterator = remaining.iterator(); iterator.hasNext();) {
                JahiaTemplatesPackageHandler handler = (JahiaTemplatesPackageHandler) iterator.next();
                JahiaTemplatesPackage pack = handler.getPackage();
                if (pack.getExtends() == null || toDeploy.containsKey(pack.getExtends())) {
                    toDeploy.put(pack.getName(), handler);
                } else {
                    newRemaining.add(handler);
                }
            }
            if (newRemaining.equals(remaining)) {
                logger.error("Cannot deploy packages " + newRemaining + ", unresolved dependencies");
                break;
            } else {
                remaining = newRemaining;
            }
        }
        return toDeploy;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setTemplatePackageRegistry(
            TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

    private boolean templatesFolderAlreadyExists(String rootFolderName) {
        return new File(settingsBean.getJahiaTemplatesDiskPath(),
                rootFolderName).exists();
    }

}