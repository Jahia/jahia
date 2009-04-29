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

import java.util.*;

import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.apache.struts.action.*;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class TestClipperForm extends JahiaAbstractWizardForm {
	private String oldSelectedUrl;
	private String selectedUrl;
	private List actifFormParamsList;
	private List actifQueryParamsList;
	private String bodyContent;
	private String webBrowserSimulatorMode;
	private String resetCache;


	/**
	 *  Constructor for the TestClipperForm object
	 */
	public TestClipperForm() {
	}



	/**
	 *  Sets the SelectedUrl attribute of the TestClipperForm object
	 *
	 *@param  selectedUrl  The new SelectedUrl value
	 */
	public void setSelectedUrl(String selectedUrl) {
		this.selectedUrl = selectedUrl;
	}


	/**
	 *  Sets the ResetCache attribute of the TestClipperForm object
	 *
	 *@param  resetCache  The new ResetCache value
	 */
	public void setResetCache(String resetCache) {
		this.resetCache = resetCache;
	}



	/**
	 *  Sets the ActifFormParamsList attribute of the TestClipperForm object
	 *
	 *@param  actifFormParamsList  The new ActifFormParamsList value
	 */
	public void setActifFormParamsList(List actifFormParamsList) {
		this.actifFormParamsList = actifFormParamsList;
	}


	/**
	 *  Sets the ActifQueryParamsList attribute of the TestClipperForm object
	 *
	 *@param  actifQueryParamsList  The new ActifQueryParamsList value
	 */
	public void setActifQueryParamsList(List actifQueryParamsList) {
		this.actifQueryParamsList = actifQueryParamsList;
	}


	/**
	 *  Sets the BodyContent attribute of the TestClipperForm object
	 *
	 *@param  bodyContent  The new BodyContent value
	 */
	public void setBodyContent(String bodyContent) {
		this.bodyContent = bodyContent;
	}


	/**
	 *  Sets the OldSelectedUrl attribute of the TestClipperForm object
	 *
	 *@param  oldSelectedUrl  The new OldSelectedUrl value
	 */
	public void setOldSelectedUrl(String oldSelectedUrl) {
		this.oldSelectedUrl = oldSelectedUrl;
	}


	/**
	 *  Sets the WebBrowserSimulatorMode attribute of the TestClipperForm object
	 *
	 *@param  webBrowserSimulatorMode  The new WebBrowserSimulatorMode value
	 */
	public void setWebBrowserSimulatorMode(String webBrowserSimulatorMode) {
		this.webBrowserSimulatorMode = webBrowserSimulatorMode;
	}


	/**
	 *  Gets the ResetCache attribute of the TestClipperForm object
	 *
	 *@return    The ResetCache value
	 */
	public String getResetCache() {
		return this.resetCache;
	}


	/**
	 *  Gets the Id attribute of the TestClipperForm object
	 *
	 *@return    The Id value
	 */
	public int getId() {
		return JahiaClipBuilderConstants.TEST;
	}


	/**
	 *  Gets the Errors attribute of the TestClipperForm object
	 *
	 *@return    The Errors value
	 */
	public ActionErrors getErrors() {
		ActionErrors errors = new ActionErrors();
		return errors;
	}


	/**
	 *  Gets the ActifFormParamsList attribute of the TestClipperForm object
	 *
	 *@return    The ActifFormParamsList value
	 */
	public List getActifFormParamsList() {
		return actifFormParamsList;
	}


	/**
	 *  Gets the SelectedUrl attribute of the TestClipperForm object
	 *
	 *@return    The SelectedUrl value
	 */
	public String getSelectedUrl() {
		return selectedUrl;
	}


	/**
	 *  Gets the ActifQueryParamsList attribute of the TestClipperForm object
	 *
	 *@return    The ActifQueryParamsList value
	 */
	public List getActifQueryParamsList() {
		return actifQueryParamsList;
	}


	/**
	 *  Gets the BodyContent attribute of the TestClipperForm object
	 *
	 *@return    The BodyContent value
	 */
	public String getBodyContent() {
		return bodyContent;
	}


	/**
	 *  Gets the OldSelectedUrl attribute of the TestClipperForm object
	 *
	 *@return    The OldSelectedUrl value
	 */
	public String getOldSelectedUrl() {
		return oldSelectedUrl;
	}


	/**
	 *  Gets the WebBrowserSimulatorMode attribute of the TestClipperForm object
	 *
	 *@return    The WebBrowserSimulatorMode value
	 */
	public String getWebBrowserSimulatorMode() {
		return webBrowserSimulatorMode;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  cBean  Description of Parameter
	 */
	public void loadFromClipperBean(ClipperBean cBean) {

	}

}
