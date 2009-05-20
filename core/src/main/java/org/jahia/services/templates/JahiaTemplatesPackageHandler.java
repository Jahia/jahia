/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//  JahiaTemplatePackageHandler
//
//  NK      13.01.2001
//
//

package org.jahia.services.templates;

import static org.jahia.services.templates.TemplateDeploymentDescriptorHelper.TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME;

import java.io.File;
import java.io.IOException;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.jahia.utils.zip.JahiaArchiveFileHandler;

/**
 * This class is responsible for loading data from a Template Jar File
 * For the format of the template deployment descriptor file, see corresponding XML schema.
 *
 * @author Khue ng
 */
public class JahiaTemplatesPackageHandler {
    
    static final String NS_URI_DEF = "http://www.w3.org/2000/xmlns/";

    static final String NS_URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    static final String NS_URI_JAHIA = "http://www.jahia.org/jahia/templates";
    
    public static final String TEMPLATES_DESCRIPTOR_20_URI = "http://www.jahia.org/shemas/templates_2_0.xsd";
    
    static final String SCHEMA_LOCATION = NS_URI_JAHIA + " "
            + TEMPLATES_DESCRIPTOR_20_URI;

    /**
     * The full path to the Template Jar File *
     */
    protected String m_FilePath;

    /**
     * The Jar File Handler of the Template Jar File *
     */
    protected JahiaArchiveFileHandler m_ArchFile;

    /**
     * The JahiaTemplatesPackage Object created with data from the templates.xml file *
     */
    protected JahiaTemplatesPackage m_Package;

    /**
     * The Jar File Handler of the classes_file.jar provided within the templates package *
     */
    protected JahiaArchiveFileHandler m_JarClassesFile;

    /**
     * Checks, if the specified directory contains a templates deployment
     * descriptor file.
     * 
     * @param dirPath
     *            the directory path to be checked
     * @return <code>true</code> if the specified directory contains a
     *         readable templates deployment descriptor file
     */
    public static boolean isValidTemplatesDirectory(String dirPath) {
        File templteDescriptor = new File(dirPath, TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME);
        return templteDescriptor.exists() && templteDescriptor.isFile()
                && templteDescriptor.canRead();
    }

    /**
     * Constructor is initialized with the template File
     */
    public JahiaTemplatesPackageHandler(String filePath)
            throws JahiaException {

        this(new File(filePath));
    }
    
    /**
     * Constructor is initialized with the template File
     */
    public JahiaTemplatesPackageHandler(File file)
            throws JahiaException {

        m_FilePath = file.getPath();

        boolean isFile = true;

        if (file.isFile() && file.canWrite()) {

            try {

                m_ArchFile = new JahiaArchiveFileHandler(m_FilePath);

            } catch (IOException e) {
                throw new JahiaTemplateServiceException(
                        "Failed creating an Archive File Handler for file: "
                                + file, e);
            }

        } else {
            isFile = false;
        }

        try {
            buildTemplatesPackage(isFile);
        } catch (JahiaException je) {

            if (m_ArchFile != null) {
                m_ArchFile.closeArchiveFile();
            }

            throw new JahiaTemplateServiceException(
                    "Error building the TemplatesPackageHandler for file: "
                            + file, je);
        }

    }

    /**
     * Extract data from the templates.xml file and build the JahiaTemplatesPackage object
     *
     * @param isFile , are we handling a file or instead a directory ?
     */
    protected void buildTemplatesPackage(boolean isFile)
            throws JahiaException {

    	// extract data from the templates.xml file
        try {
            if (isFile) {
                File tmpFile = m_ArchFile.extractFile(TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME);
                m_Package = new Templates_Xml(tmpFile.getAbsolutePath()).getTemplatesPackage();
                tmpFile.deleteOnExit();
                tmpFile.delete();
            } else {
                m_Package = new Templates_Xml(m_FilePath + File.separator
						+ TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME).getTemplatesPackage();
            }
        } catch (IOException ioe) {
            throw new JahiaTemplateServiceException("Failed extracting templates.xml file data", ioe);
        }

        m_Package.setFilePath(m_FilePath);
    }

    /**
     * Returns the Generated JahiaTemplatesPackage Object
     *
     * @return (JahiaTemplatesPackage) the package object
     */
    public JahiaTemplatesPackage getPackage() {

        return m_Package;
    }

    /**
     * Unzip the contents of the jar file in it's current folder
     */
    public void unzip()
            throws JahiaException {

        // Unzip the file
        m_ArchFile.unzip();

    }

    /**
     * Unzip the contents of the jar file in a gived folder
     *
     * @param (String) path , the path where to extract file
     */
    public void unzip(String path)
            throws JahiaException {

        // Unzip the file
        m_ArchFile.unzip(path);
    }

    /**
     * Unzip the classes file in a gived folder
     *
     * @param (String) path , the path where to extract file
     */
    public void unzipClassesFile(String path)
            throws JahiaException {

        // Unzip the file
        //m_JarClassesFile.extractEntry(CLASSES_FILE_ENTRY, path);
        if (m_JarClassesFile != null) {
            m_JarClassesFile.unzip(path);
        }
    }

    /**
     * Unzip an entry in a gived folder
     *
     * @param (String) entryName , the name of the entry
     * @param (String) path , the path where to extract file
     */
    public void extractEntry(String entryName,
                             String path)
            throws JahiaException {

        // Unzip the entry
        m_ArchFile.extractEntry(entryName, path);
    }

    /**
     * Close the Jar file
     */
    public void closeArchiveFile() {

        m_ArchFile.closeArchiveFile();
        if (m_ArchFile != null) {
            m_ArchFile.closeArchiveFile();
        }
    }

    public static JahiaTemplatesPackageHandler getPackageHandler(File file) throws JahiaException {
        if (file.getName().endsWith(".jar")) {
            return new JahiaTemplatesPackageHandler(file);
        } else if (file.getName().endsWith(".war")) {
            return new JahiaTemplatesWarPackageHandler(file);
        }
        return null;
    }
}