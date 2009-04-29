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
