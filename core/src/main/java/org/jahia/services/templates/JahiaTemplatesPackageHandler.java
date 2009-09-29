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

import static org.jahia.services.templates.TemplateDeploymentDescriptorHelper.TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.compass.core.util.reader.StringReader;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaArchiveFileException;
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
     * The file representing template set archive or folder
     */
    private File file;

    /**
     * The Jar File Handler of the Template Jar File
     */
    private JahiaArchiveFileHandler archive;
    
    /**
     * The JahiaTemplatesPackage Object created with data from the templates.xml file
     */
    private JahiaTemplatesPackage templatePackage;

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
    public JahiaTemplatesPackageHandler(File file)
            throws JahiaException {

        this.file = file;
        
    	if (file.isFile() && file.canWrite()) {
            try {
                archive = new JahiaArchiveFileHandler(file.getPath());
            } catch (IOException e) {
                throw new JahiaTemplateServiceException(
                        "Failed creating an Archive File Handler for file: "
                                + file, e);
            }
        }

        try {
            buildTemplatesPackage();
        } catch (JahiaException je) {
            throw new JahiaTemplateServiceException(
                    "Error building the TemplatesPackageHandler for file: "
                            + file, je);
        } finally {
            if (archive != null) {
                archive.closeArchiveFile();
            }
        }

    }

    /**
     * Extract data from the templates.xml file and build the JahiaTemplatesPackage object
     */
    private void buildTemplatesPackage()
            throws JahiaException {

    	// extract data from the templates.xml file
        try {
			Reader contentReader = file.isFile() ? new StringReader(archive
			        .entryExists(TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME) ? archive
			        .extractContent(TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME) : archive.extractContent("WEB-INF/"
			        + TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME)) : new FileReader(new File(file.getPath(),
			        TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME));
        	// extract data from the templates.xml file
            templatePackage = TemplateDeploymentDescriptorHelper.parse(contentReader);
        } catch (IOException ioe) {
            throw new JahiaTemplateServiceException("Failed extracting templates.xml file data", ioe);
        } catch (JahiaArchiveFileException ex) {
        	if (ex.getErrorCode() != JahiaException.ENTRY_NOT_FOUND) {
        		throw ex;
        	}
        }
        postProcess();
    }

    private void postProcess() {
        if (templatePackage == null) {
        	templatePackage = new JahiaTemplatesPackage();
        }
        
        templatePackage.setFilePath(file.getPath());
        
        if (StringUtils.isEmpty(templatePackage.getName())) {
        	templatePackage.setName(FilenameUtils.getBaseName(file.isFile() ? archive.getPath() : file.getPath()));
        }
        if (StringUtils.isEmpty(templatePackage.getRootFolder())) {
        	templatePackage.setRootFolder((FilenameUtils.getBaseName(file.isFile() ? archive.getPath() : file.getPath()).replace('-', '_')).toLowerCase());
        }
        if (file.isFile()) {
	        if (templatePackage.getDefinitionsFiles().isEmpty()) {
	        	// check if there is a definitions file
	        	if (archive.entryExists("definitions.cnd")) {
	        		templatePackage.setDefinitionsFile("definitions.cnd");
	        	}
	        	// check if there is a definitions grouping file
	        	if (archive.entryExists("definitions.grp")) {
	        		templatePackage.setDefinitionsFile("definitions.grp");
	        	}
	        }
	        if (templatePackage.getRulesFiles().isEmpty()) {
	        	// check if there is a rules file
	        	if (archive.entryExists("rules.dsl")) {
	        		templatePackage.setRulesFile("rules.dsl");
	        	}
	        }
	        if (templatePackage.getResourceBundleName() == null) {
	        	// check if there is a resource bundle file in the resources folder
	        	String rbName = templatePackage.getName().replace(' ', '_');
	        	if (archive.entryExists("resources/" + rbName + ".properties")) {
	        		templatePackage.setResourceBundleName("resources." + rbName);
	        	} else {
	        		rbName = templatePackage.getName().replace(" ", "");
	            	if (archive.entryExists("resources/" + rbName + ".properties")) {
	            		templatePackage.setResourceBundleName("resources." + rbName);
	            	}        		
	        	}
	        }
        }
    }

	/**
     * Returns the Generated JahiaTemplatesPackage Object
     *
     * @return (JahiaTemplatesPackage) the package object
     */
    public JahiaTemplatesPackage getPackage() {
        return templatePackage;
    }

    /**
     * Unzip the contents of the jar file in a given folder
     *
     * @param (String) path , the path where to extract file
     */
    public void unzip(String path)
            throws JahiaException {

        // Unzip the file
        archive.unzip(path);
    }

	/**
	 * Returns the file descriptor, representing this template package.
	 * 
	 * @return the file descriptor, representing this template package
	 */
	public File getFile() {
		return file;
	}
}