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
 package org.jahia.clipbuilder.html.struts;

import org.apache.struts.action.*;
import org.jahia.clipbuilder.html.struts.Util.JahiaAbstractWizardForm;
import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants;

/**
 *  From related whith manageClippers.jsp
 *
 *@author    Tlili Khaled
 */
public class ManageClippersForm extends JahiaAbstractWizardForm {
	private String clipper;
	private boolean templateView;


	/**
	 *  Sets the Clipper attribute of the ManageClippersForm object
	 *
	 *@param  clipper  The new Clipper value
	 */
	public void setClipper(String clipper) {
		this.clipper = clipper;
	}

        public int getId() {
                return JahiaClipBuilderConstants.MANAGE;
        }



	/**
	 *  Sets the TemplateView attribute of the ManageClippersForm object
	 *
	 *@param  templateView  The new TemplateView value
	 */
	public void setTemplateView(boolean templateView) {
		this.templateView = templateView;
	}


	/**
	 *  Gets the Clipper attribute of the ManageClippersForm object
	 *
	 *@return    The Clipper value
	 */
	public String getClipper() {
		return clipper;
	}


	/**
	 *  Gets the TemplateView attribute of the ManageClippersForm object
	 *
	 *@return    The TemplateView value
	 */
	public boolean isTemplateView() {
		return templateView;
	}


	/**
	 *  Gets the Errors attribute of the ManageClippersForm object
	 *
	 *@return    The Errors value
	 */
	public ActionErrors getErrors() {
		ActionErrors errors = new ActionErrors();
		return errors;
	}



	/**
	 *  Description of the Method
	 *
	 *@param  cBean  Description of Parameter
	 */
	public void loadFromClipperBean(ClipperBean cBean) {
	}
}
