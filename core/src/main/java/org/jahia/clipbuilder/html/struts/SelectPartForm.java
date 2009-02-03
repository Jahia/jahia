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

import javax.servlet.http.*;

import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.apache.struts.action.*;

/**
 *  Form linked with selectPart.jsp
 *
 *@author    Tlili Khaled
 */
public class SelectPartForm extends JahiaAbstractWizardForm {

	private String clippingMethod = "1";
	private String bodyContent;
	private String targetUrl;
	private String selectedContent;
	private String webClippingTypeContent;
	private String webClippingPartHashCode;
	private String webClippingTagName;
	private String webClippingXPath = "//form";
	private String webClippingShowCss = "true";
	private String webClippingShowJavascript;


	/**
	 *  Sets the BodyContent attribute of the SelectPartForm object
	 *
	 *@param  bodyContent  The new BodyContent value
	 */
	public void setBodyContent(String bodyContent) {
		this.bodyContent = bodyContent;
	}



	/**
	 *  Sets the ClippingMethod attribute of the SelectPartForm object
	 *
	 *@param  clippingMethod  The new ClippingMethod value
	 */
	public void setClippingMethod(String clippingMethod) {
		this.clippingMethod= clippingMethod;
	}



	/**
	 *  Sets the TargetUrl attribute of the SelectPartForm object
	 *
	 *@param  targetUrl  The new TargetUrl value
	 */
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}


	/**
	 *  Sets the SelectedContent attribute of the SelectPartForm object
	 *
	 *@param  selectedContent  The new SelectedContent value
	 */
	public void setSelectedContent(String selectedContent) {
		this.selectedContent = selectedContent;
	}


	/**
	 *  Sets the WebClippingTypeContent attribute of the SelectPartForm object
	 *
	 *@param  webClippingTypeContent  The new WebClippingTypeContent value
	 */
	public void setWebClippingTypeContent(String webClippingTypeContent) {
		this.webClippingTypeContent = webClippingTypeContent;
	}


	/**
	 *  Sets the WebClippingPartHashCode attribute of the SelectPartForm object
	 *
	 *@param  webClippingPartHashCode  The new WebClippingPartHashCode value
	 */
	public void setWebClippingPartHashCode(String webClippingPartHashCode) {
		this.webClippingPartHashCode = webClippingPartHashCode;
	}


	/**
	 *  Sets the WebClippingTagName attribute of the SelectPartForm object
	 *
	 *@param  webClippingTagName  The new WebClippingTagName value
	 */
	public void setWebClippingTagName(String webClippingTagName) {
		this.webClippingTagName = webClippingTagName;
	}


	/**
	 *  Sets the WebClippingXPath attribute of the SelectPartForm object
	 *
	 *@param  webClippingXPath  The new WebClippingXPath value
	 */
	public void setWebClippingXPath(String webClippingXPath) {
		this.webClippingXPath = webClippingXPath;
	}


	/**
	 *  Gets the Id attribute of the SelectPartForm object
	 *
	 *@return    The Id value
	 */
	public int getId() {
		return JahiaClipBuilderConstants.SELECTPART;
	}


	/**
	 *  Gets the Errors attribute of the SelectPartForm object
	 *
	 *@return    The Errors value
	 */
	public ActionErrors getErrors() {
		ActionErrors errors = new ActionErrors();
		return errors;
	}


	/**
	 *  Gets the BodyContent attribute of the SelectPartForm object
	 *
	 *@return    The BodyContent value
	 */
	public String getBodyContent() {
		return bodyContent;
	}


	/**
	 *  Gets the ClippingMethod attribute of the SelectPartForm object
	 *
	 *@return    The ClippingMethod value
	 */
	public String getClippingMethod() {
		return clippingMethod;
	}


	/**
	 *  Gets the TargetUrl attribute of the SelectPartForm object
	 *
	 *@return    The TargetUrl value
	 */
	public String getTargetUrl() {
		return targetUrl;
	}


	/**
	 *  Gets the SelectedContent attribute of the SelectPartForm object
	 *
	 *@return    The SelectedContent value
	 */
	public String getSelectedContent() {
		return selectedContent;
	}


	/**
	 *  Gets the WebClippingTypeContent attribute of the SelectPartForm object
	 *
	 *@return    The WebClippingTypeContent value
	 */
	public String getWebClippingTypeContent() {
		return webClippingTypeContent;
	}


	/**
	 *  Gets the WebClippingPartHashCode attribute of the SelectPartForm object
	 *
	 *@return    The WebClippingPartHashCode value
	 */
	public String getWebClippingPartHashCode() {
		return webClippingPartHashCode;
	}


	/**
	 *  Gets the WebClippingTagName attribute of the SelectPartForm object
	 *
	 *@return    The WebClippingTagName value
	 */
	public String getWebClippingTagName() {
		return webClippingTagName;
	}


	/**
	 *  Gets the WebClippingXPath attribute of the SelectPartForm object
	 *
	 *@return    The WebClippingXPath value
	 */
	public String getWebClippingXPath() {
		return webClippingXPath;
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


	/**
	 *  Sets the WebClippingShowCss attribute of the SelectPartForm object
	 *
	 *@param  value  The new WebClippingShowCss value
	 */
	public void setWebClippingShowCss(String value) {
		this.webClippingShowCss = value;
	}


	/**
	 *  Sets the WebClippingShowJavascript attribute of the SelectPartForm object
	 *
	 *@param  value  The new WebClippingShowJavascript value
	 */
	public void setWebClippingShowJavascript(String value) {
		this.webClippingShowJavascript = value;
	}


	/**
	 *  Gets the WebClippingShowCss attribute of the SelectPartForm object
	 *
	 *@return    The WebClippingShowCss value
	 */
	public String getWebClippingShowCss() {
		return webClippingShowCss;
	}


	/**
	 *  Gets the WebClippingShowJavascript attribute of the SelectPartForm object
	 *
	 *@return    The WebClippingShowJavascript value
	 */
	public String getWebClippingShowJavascript() {
		return webClippingShowJavascript;
	}

}
