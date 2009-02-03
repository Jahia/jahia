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
import javax.servlet.http.*;
import org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants;

/**
 *  Bean linked to previewClipper.jsp
 *
 *@author    Tlili Khaled
 */
public class PreviewForm
		 extends JahiaAbstractWizardForm {
	private String selectedPart = "<p> Enable to clip the selected content. </p>";
	private String from;


	/**
	 *  Sets the SelectedPart attribute of the PreviewForm object
	 *
	 *@param  selectedPart  The new SelectedPart value
	 */
	public void setSelectedPart(String selectedPart) {
		this.selectedPart = selectedPart;
	}

        public int getId() {
                return JahiaClipBuilderConstants.PREVIEW;
        }



	/**
	 *  Sets the From attribute of the PreviewForm object
	 *
	 *@param  from  The new From value
	 */
	public void setFrom(String from) {
		this.from = from;
	}


	/**
	 *  Gets the Errors attribute of the PreviewForm object
	 *
	 *@return    The Errors value
	 */
	public ActionErrors getErrors() {
		ActionErrors errors = new ActionErrors();
		return errors;
	}


	/**
	 *  Gets the SelectedPart attribute of the PreviewForm object
	 *
	 *@return    The SelectedPart value
	 */
	public String getSelectedPart() {
		return selectedPart;
	}


	/**
	 *  Gets the From attribute of the PreviewForm object
	 *
	 *@return    The From value
	 */
	public String getFrom() {
		return from;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping       Description of Parameter
	 *@param  httpServletRequest  Description of Parameter
	 *@return                     Description of the Returned Value
	 */
	public ActionErrors validate(ActionMapping actionMapping,
			HttpServletRequest httpServletRequest) {

		return null;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping       Description of Parameter
	 *@param  httpServletRequest  Description of Parameter
	 */
	public void reset(ActionMapping actionMapping,
			HttpServletRequest httpServletRequest) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  cBean  Description of Parameter
	 */
	public void loadFromClipperBean(ClipperBean cBean) {

	}

}
