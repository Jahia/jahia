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
package org.jahia.services.templates;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import static org.jahia.services.templates.TemplateDeploymentDescriptorHelper.TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 22, 2008
 * Time: 6:00:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaTemplatesWarPackageHandler extends JahiaTemplatesPackageHandler {
    public JahiaTemplatesWarPackageHandler(String filePath) throws JahiaException {
        super(filePath);
    }

    public JahiaTemplatesWarPackageHandler(File file) throws JahiaException {
        super(file);
    }


    protected void buildTemplatesPackage(boolean isFile)
            throws JahiaException {

    	// extract data from the templates.xml file
        try {
            if (isFile) {
                File tmpFile = m_ArchFile.extractFile("WEB-INF/"+TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME);
                m_Package = new Templates_Xml(tmpFile.getAbsolutePath()).getTemplatesPackage();
                tmpFile.deleteOnExit();
                tmpFile.delete();
            } else {
                m_Package = new Templates_Xml(m_FilePath + File.separator + "WEB-INF/"
						+ TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME).getTemplatesPackage();
            }
        } catch (IOException ioe) {
            throw new JahiaTemplateServiceException("Failed extracting templates.xml file data", ioe);
        }

        m_Package.setFilePath(m_FilePath);
    }

}
