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
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaArchiveFileException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import static org.jahia.services.templates.TemplateDeploymentDescriptorHelper.TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
     * The JahiaTemplatesPackage Object created with data from the templates.xml file
     */
    private JahiaTemplatesPackage templatePackage;

    /**
     * Constructor is initialized with the template File
     */
    public JahiaTemplatesPackageHandler(File file)
            throws JahiaException {

        this.file = file;
        
        try {
            buildTemplatesPackage();
        } catch (JahiaException je) {
            throw new JahiaTemplateServiceException(
                    "Error building the TemplatesPackageHandler for file: "
                            + file, je);
        }

    }

    /**
     * Extract data from the templates.xml file and build the JahiaTemplatesPackage object
     */
    private void buildTemplatesPackage()
            throws JahiaException {

    	// extract data from the templates.xml file
        try {
            File xml = new File(file,TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME);
            if (!xml.exists()) {
                xml = new File(file,"WEB-INF/"+TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME);
            }
            if (xml.exists()) {
            Reader contentReader = new FileReader(xml);
            // extract data from the templates.xml file
                templatePackage = TemplateDeploymentDescriptorHelper.parse(contentReader);
            } else {
                templatePackage = new JahiaTemplatesPackage();
            }
            File manifestFile = new File(file, "META-INF/MANIFEST.MF");
            if (manifestFile.exists()) {
                Manifest manifest = new Manifest(new FileInputStream(manifestFile));
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

                templatePackage.setName(packageName);
                templatePackage.setRootFolder(rootFolder);
            }
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
        	templatePackage.setName(FilenameUtils.getBaseName(file.getPath()));
        }
        if (StringUtils.isEmpty(templatePackage.getRootFolder())) {
        	templatePackage.setRootFolder((FilenameUtils.getBaseName(file.getPath()).replace('-', '_')).toLowerCase());
        }
        if (templatePackage.getDefinitionsFiles().isEmpty()) {
            // check if there is a definitions file
            if (new File(file,"definitions.cnd").exists()) {
                templatePackage.setDefinitionsFile("definitions.cnd");
            }
            // check if there is a definitions grouping file
            if (new File(file,"definitions.grp").exists()) {
                templatePackage.setDefinitionsFile("definitions.grp");
            }
        }
        if (templatePackage.getRulesFiles().isEmpty()) {
            // check if there is a rules file
            if (new File(file,"rules.dsl").exists()) {
                templatePackage.setRulesFile("rules.dsl");
            }
        }
        if (templatePackage.getResourceBundleName() == null) {
            // check if there is a resource bundle file in the resources folder
            String rbName = templatePackage.getName().replace(' ', '_');
            if (new File(file,"resources/" + rbName + ".properties").exists()) {
                templatePackage.setResourceBundleName("resources." + rbName);
            } else {
                rbName = templatePackage.getName().replace(" ", "");
                if (new File(file, "resources/" + rbName + ".properties").exists()) {
                    templatePackage.setResourceBundleName("resources." + rbName);
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
	 * Returns the file descriptor, representing this template package.
	 * 
	 * @return the file descriptor, representing this template package
	 */
	public File getFile() {
		return file;
	}
}