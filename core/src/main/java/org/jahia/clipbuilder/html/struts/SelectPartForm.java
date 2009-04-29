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
