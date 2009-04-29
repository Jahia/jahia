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