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
