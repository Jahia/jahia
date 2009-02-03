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
