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
package org.jahia.services.templates;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.xml.JahiaXmlDocument;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.w3c.dom.Element;

/**
 * Performs parsing of the template deployment descriptor file (templates.xml).
 * 
 * @author Khue ng
 */
final class Templates_Xml extends JahiaXmlDocument {

	private JahiaTemplatesPackage templatesPackage;
	
	/**
	 * Constructor
	 * 
	 * @param (String)
	 *            path, the full path to the templates.xml file
	 */
	public Templates_Xml(String docPath) throws JahiaException {
		super(docPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jahia.data.xml.JahiaXmlDocument#extractDocumentData()
	 */
	public void extractDocumentData() throws JahiaException {
		if (m_XMLDocument == null) {

			throw new JahiaException("Templates_Xml",
					"Parsed templates.xml document is null",
					JahiaException.ERROR_SEVERITY, JahiaException.SERVICE_ERROR);
		}

		if (!m_XMLDocument.hasChildNodes()) {

			throw new JahiaException("templates_Xml",
					"Main document node has no children",
					JahiaException.ERROR_SEVERITY, JahiaException.SERVICE_ERROR);
		}

		// get the document node
		Element docElNode = (Element) m_XMLDocument.getDocumentElement();
		if ("tpml".equals(docElNode.getNodeName())) {
		    templatesPackage = TemplateDeploymentDescriptorHelper.parseLegacyFormat(m_XMLDocument);
		} else if ("template-set".equals(docElNode.getNodeName())) {
            templatesPackage = TemplateDeploymentDescriptorHelper.parse(
                    m_DocPath, this.getClass().getClassLoader());
		} else {
		    throw new JahiaTemplateServiceException(
                    "Unknown format of the templates deployment descriptor: "
                            + m_DocPath);
		}
	}
	
	/**
	 * Returns the the template package descriptor.
	 * 
	 * @return the template package descriptor
	 * @throws JahiaException
	 *             in case of a parsing error
	 */
	public JahiaTemplatesPackage getTemplatesPackage() throws JahiaException {
		if (templatesPackage == null)
			extractDocumentData();

		return templatesPackage;
	}
}