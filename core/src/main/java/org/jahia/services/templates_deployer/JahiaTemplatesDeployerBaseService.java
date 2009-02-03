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

//
//
//  JahiaTemplatesDeployerBaseService
//
//  NK      12.01.2001
//
//


package org.jahia.services.templates_deployer;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplateDef;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.deamons.filewatcher.FileListSync;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.JahiaTemplatesPackageHandler;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.zip.JahiaArchiveFileHandler;


/**
 * This Service Deploy templates packed in a .jar file.
 *
 * @author Khue ng
 * @version 1.0
 * @deprecated due to changes in the template deployment
 */
public class JahiaTemplatesDeployerBaseService extends JahiaTemplatesDeployerService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaTemplatesDeployerBaseService.class);

    private static JahiaTemplatesDeployerBaseService m_Instance = null;

    /**
     * The relative Path to the Template Root Folder *
     */
    private static String m_TemplateRootPath = "";

    /**
     * The Shared Templates Path *
     */
    private static String m_SharedTemplatesPath = "";

    /**
     * The JSP context path *
     */
    private static String m_JspContext = "";

    /**
     * The templates context path *
     */
    private static String m_TemplatesContext = "";

    /**
     * The relative Path to the New Template Folder *
     */
    private static String m_NewTemplatePath = "";

    /**
     * The classes Root Path *
     */
    private static String m_ClassesRootPath;

    /**
     * The templates classe Root Path *
     */
    private static String m_TEMPLATES_CLASSES_ROOT_FOLDER = "jahiatemplates";


    /**
     * Meta-Inf folder *
     */
    protected static final String m_META_INF = "Meta-Inf";


    /**
     * Constructor
     */
    protected JahiaTemplatesDeployerBaseService() {
    }


    /**
     * Use this method to get an instance of this class
     */
    public static JahiaTemplatesDeployerBaseService getInstance(){

        if (m_Instance == null) {
            synchronized (JahiaTemplatesDeployerBaseService.class) {
                if ( m_Instance == null ){
                    m_Instance = new JahiaTemplatesDeployerBaseService();
                }
            }
        }
        return m_Instance;
    }


    /***
     *
     */
    public void start()
            throws JahiaInitializationException {

        m_TemplateRootPath = settingsBean.getJahiaTemplatesDiskPath();
        m_JspContext = settingsBean.getJspContext();
        m_TemplatesContext = settingsBean.getTemplatesContext();
        m_ClassesRootPath = settingsBean.getClassDiskPath() + File.separator + m_TEMPLATES_CLASSES_ROOT_FOLDER;
        m_NewTemplatePath = settingsBean.getJahiaNewTemplatesDiskPath();
        m_SharedTemplatesPath = settingsBean.getJahiaSharedTemplatesDiskPath();

        logger.debug("m_TemplateRootPath=" + m_TemplateRootPath);
        logger.debug("m_JspContext=" + m_JspContext);
        logger.debug("m_TemplatesContext" + m_TemplatesContext);
        logger.debug("m_ClassesRootPath=" + m_ClassesRootPath);

        // create the temporary folder
        File f = new File(m_NewTemplatePath);
        File parent = f.getParentFile();
        if (parent != null) {
            File tmpFolder = new File(parent.getAbsolutePath() + File.separator + "tmp");
            tmpFolder.mkdirs();
            if (tmpFolder == null || !tmpFolder.isDirectory()) {
                String errMsg = " cannot create a temporaty folder ";
                logger.error(errMsg + "\n");
                throw new JahiaInitializationException(errMsg);
            }
            m_TempFolderDiskPath = tmpFolder.getAbsolutePath();
        }

        // create the shared templates folder
        File sTemplates = new File(m_SharedTemplatesPath);
        sTemplates.mkdirs();

    } // end init

    public void stop() {
    }

    /**
     * Deploy a template .jar file.
     * The jar file will be unzipped in the root folder.
     * <p/>
     * If the RootFolder is equals to an empty string, the Root Folder
     * is the one described in the templates.xml
     *
     * @param (JahiaSite) the site
     * @param (String)    rootFolder, root folder for the template
     * @param (String)    filePath , the full path to the template.jar file
     * @param (boolean)   moveTemplate , if true, move te template package to the deployed directory when done
     */
    public boolean deploy(JahiaSite site, String rootFolder, String filePath, boolean moveTemplate) throws JahiaException {

        boolean success = false;
        String fullPath = null;

        if (filePath.endsWith(".jar")) {

            JahiaTemplatesPackageHandler ph = null;

            try {

                logger.debug("Trying to deploy " + filePath + "...");

                ph = new JahiaTemplatesPackageHandler(filePath);

                JahiaTemplatesPackage tempPack = ph.getPackage();

                if (tempPack == null) {
                    String errMsg = " cannot create a JahiaTemplatesPackage on file " + filePath;
                    logger.debug(errMsg);
                    throw new JahiaException("JahiaTemplatesDeployerBaseService::deploy()", "JahiaTemplatesDeployerBaseService" + errMsg,
                            JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY);
                }

                StringBuffer strBuf = new StringBuffer(1024);
                strBuf.append(m_TemplateRootPath);
                strBuf.append(File.separator);
                strBuf.append(site.getSiteKey());
                strBuf.append(File.separator);
                if (rootFolder.equals("")) {
                    if (tempPack.getRootFolder() != null) {
                        strBuf.append(tempPack.getRootFolder());
                    }
                } else {
                    strBuf.append(rootFolder);
                }

                // Full path to the templates root folder
                fullPath = strBuf.toString();

                // Check that the root folder for the new template doesn't exist
                File f = new File(fullPath);
                if (f != null && !f.isDirectory()) {
                    // create the dir
                    f.mkdirs();
                }
                logger.debug("Delete meta-inf folder=" + f.getAbsolutePath());
                // Remove the Meta-Inf folder first
                removeMetaInfFolder(f.getAbsolutePath());

                ph.unzip(fullPath);

                // extract the classes file
                if (tempPack.hasClasses()) {
                    String classesFilePath = fullPath + File.separator + tempPack.getClassesFile();

                    File tmpFile = new File(classesFilePath);
                    try {
                        if (tmpFile != null && tmpFile.isFile()) {
                            JahiaArchiveFileHandler arch = new JahiaArchiveFileHandler(classesFilePath);
                            ImportExportBaseService importExportService = ImportExportBaseService.getInstance();
                            arch
                                    .unzip(
                                            tempPack.getClassesRoot() == null ? m_ClassesRootPath
                                                    : settingsBean
                                                            .getClassDiskPath()
                                                            + File.separator
                                                            + tempPack
                                                                    .getClassesRoot(),
                                            importExportService
                                                    .isOverwriteResourcesByImport(),
                                            importExportService
                                                    .getExcludedResourcesFilter(),
                                            tempPack.getClassesRoot() != null ? tempPack
                                                    .getClassesRoot()
                                                    : m_TEMPLATES_CLASSES_ROOT_FOLDER);
                        } else {
                            success = false;
                        }
                    } catch (IOException ioe) {
                        String errMsg = "Failed creating JahiaArchiveFileHandler on classes file ";
                        logger.error(errMsg, ioe);
                        throw new JahiaException("JahiaTemplatesDeployerBaseService::deploy()", "JahiaTemplatesDeployerBaseService" + errMsg,
                                JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, ioe);
                    }
                }
                success = true;
                FileListSync.getInstance().trigger();
            } catch (Exception t) {

                String errMsg = "Failed handling templates file ";
                logger.debug(errMsg, t);
                throw new JahiaException("JahiaTemplatesDeployerBaseService::deploy()", "JahiaTemplatesDeployerBaseService" + errMsg,
                        JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, t);
            } finally {
                if (ph != null) {
                    ph.closeArchiveFile();
                }
            }

            if (success && moveTemplate) {

                // move the template package into the template root folder
                File fileItem = new File(filePath);
                fileItem.renameTo(new File(fullPath + File.separator + fileItem.getName()));
            }
        }

        return success;
    }


    /**
     * Undeploy a template
     *
     * @param (JahiaPageDefinition) template
     */
    public boolean undeploy(JahiaPageDefinition template) throws JahiaException {

        /*
        if ( template != null ){

            File f = new File(m_JspContext + File.sepatator

            // Delete physically the directory on disk
            JahiaTools.deleteFile(new File(m_WebAppRootPath + app.getContext()));
            return true;
        }
        */
        return false;

    }


    /**
     * Deploy a List of .jar templates files
     *
     * @param (JahiaSite) the site
     * @param (List)    files, a List of .jar templates package file
     * @return (boolean) false on exception
     */
    public boolean deploy(JahiaSite site, List files) {

        synchronized (files) {

            if (site == null) {
                return false;
            }

            if (!Jahia.isInitiated()) {
                return false;
            }

            int size = files.size();
            File fileItem = null;

            for (int i = 0; i < size; i++) {

                fileItem = (File) files.get(i);

                if (fileItem.isFile() && fileItem.getName().endsWith(".jar")) {

                    /*
                    logger.debug(" JahiaTemplatesDeployerBaseService found new template: "
                                            + fileItem.getName() );
                    */
                    try {

                        int templateLimit = Jahia.getTemplateLimit();

                        if (site.getTemplatesAutoDeployMode()) {
                            JahiaTemplatesPackage pack = loadTemplatesInfo(fileItem.getAbsolutePath());
                            if (pack != null) {

                                // check for license limitation
                                int nbTemplates = ServicesRegistry.getInstance()
                                        .getJahiaPageTemplateService()
                                        .getNbPageTemplates(site.getID());

                                if ((Jahia.getTemplateLimit() != -1)
                                        && (nbTemplates + pack.getTemplates().size()) >
                                        templateLimit) {
                                    site.setTemplatesAutoDeployMode(false);

                                    ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);

                                    addNewFile(site, fileItem.getAbsolutePath());
                                } else {

                                    if (deploy(site, "", fileItem.getAbsolutePath(), true)) {
                                        // register in Jahia
                                        registerTemplates(site, pack);
                                        deletePackage(site, fileItem.getAbsolutePath());
                                    } else {
                                        fileItem.delete();
                                        deletePackage(site, fileItem.getAbsolutePath());
                                        return false;
                                    }
                                }

                            } else {
                                try {
                                    File newFile = new File(fileItem.getAbsolutePath() + "_error");
                                    //newFile.createNewFile();
                                    fileItem.renameTo(newFile);
                                } catch (Exception t) {
                                    //logger.debug("Exception while trying to rename error file ");
                                }
                            }
                        } else {
                            addNewFile(site, fileItem.getAbsolutePath());
                        }
                    } catch (JahiaException e) {
                        String errMsg = "Failed deploying file ";
                        logger.error(errMsg, e);
                        fileItem.delete();
                        return false;
                    }

                }
            }
            return true;
        }
    }


    /**
     * Try to load info from a .jar template file or an unzipped directory
     * Informations are read from the templates.xml file
     *
     * @param (String) full path to a file or directory
     * @return a  JahiaTemplatesPackage object or null
     */
    public JahiaTemplatesPackage loadTemplatesInfo(String path) {

        JahiaTemplatesPackage pack = null;

        // check for case sensitive
        if (!JahiaTools.checkFileNameCaseSensitive(path)) {
            return null;
        }

        File f = new File(path);

        try {
            // wait while the file is still modified
            long fLength = f.length();
            Thread.sleep(500);
            while (fLength != f.length()) {
                fLength = f.length();
                Thread.sleep(500);
            }
        } catch (Exception tr) {
            return null;
        }


        if (f != null && (f.isFile() || f.isDirectory())) {

            JahiaTemplatesPackageHandler ph = null;
            try {
                ph = new JahiaTemplatesPackageHandler(path);
                pack = ph.getPackage();
                return pack;
            } catch (JahiaException je) {
                // FIXME not critial
                //logger.debug("Error scanning template in " + path + " , " + je.getMessage());
                return null;
            } finally {
                if (ph != null) {
                    ph.closeArchiveFile();
                }
            }
        }

        return pack;
    }


    /**
     * Register templates in Jahia
     *
     * @param int                   site id , which site
     * @param JahiaTemplatesPackage the templates package
     */
    public void registerTemplates(JahiaSite site, JahiaTemplatesPackage pack) throws JahiaException {

        //logger.debug("registerTemplates(), started ");

        //logger.debug("registerTemplates(), for site " + site.getServerName() );

        // save definition in db
        int size = pack.getTemplates().size();

        StringBuffer tempFullPath = new StringBuffer(1024);
        tempFullPath.append(m_TemplatesContext);
        tempFullPath.append(site.getSiteKey());
        tempFullPath.append("/");
        if (pack.getRootFolder().length()>0) {
            tempFullPath.append(pack.getRootFolder());
            tempFullPath.append("/") ;
        }

        for (int i = 0; i < size; i++) {

            JahiaTemplateDef tempDef = (JahiaTemplateDef) pack.getTemplates().get(i);

            //logger.debug("try to add template " +  tempDef.getDisplayName());

            JahiaPageDefinition pageDef = ServicesRegistry.getInstance().getJahiaPageTemplateService().
                    getPageTemplateBySourcePath(site.getID(), tempFullPath.toString() + tempDef.getFileName());

            if (pageDef == null || (!pageDef.getName().equals(tempDef.getDisplayName()))) {

                ServicesRegistry.getInstance().getJahiaPageTemplateService().
                        createPageTemplate(
                                site.getID(),
                                tempDef.getDisplayName(),
                                tempFullPath.toString() + tempDef.getFileName(),
                                tempDef.isVisible(),
                                tempDef.getPageType(),
                                tempDef.getDescription(),
                                "",      // no image
                                site.getAclID());

                logger.debug("Added template " + tempDef.getFileName());
            }
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Return the template root path
     *
     * @return String the template root path
     */
    public String getTemplateRootPath() {
        return m_TemplateRootPath;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the template jsp context
     *
     * @return String the template jsp context
     */
    public String getTemplatesContext() {
        return m_TemplatesContext;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the new templates root path
     *
     * @return String the new templates root path
     */
    public String getNewTemplatesPath() {
        return m_NewTemplatePath;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the shared templates folder disk path
     *
     * @return String the shared templates folder disk path
     */
    public String getSharedTemplatesPath() {
        return m_SharedTemplatesPath;
    }


    //-------------------------------------------------------------------------
    /**
     * Search and remove the Meta-Inf folder
     *
     * @param String parentPath, the path of the parent Folder
     * @return (boolean) true if successfull
     */
    protected boolean removeMetaInfFolder(String parentPath) {

        if (parentPath == null) {
            return false;
        }

        File f = new File(parentPath);
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equalsIgnoreCase(m_META_INF)) {
                try {
                    return JahiaTools.deleteFile(files[i], false);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            }
        }

        return false;
    }

}

