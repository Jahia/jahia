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

//import org.jahia.clipbuilder.html.struts.wizard.*;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class BrowseForm extends JahiaAbstractWizardForm {
	private String sourceUrl;
	private String from;
	private String linkHash;
	private String state;
	private String bodyContent;
	private String continueBrowsing;
	private String httpMethod;
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BrowseForm.class);


	/**
	 *  Sets the SourceUrl attribute of the BrowseForm object
	 *
	 *@param  sourceUrl  The new SourceUrl value
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}


	/**
	 *  Sets the Hash attribute of the BrowseForm object
	 *
	 *@param  linkHash  The new LinkHash value
	 */
	public void setLinkHash(String linkHash) {
		this.linkHash = linkHash;
	}


	/**
	 *  Sets the From attribute of the BrowseForm object
	 *
	 *@param  from  The new From value
	 */
	public void setFrom(String from) {
		this.from = from;
	}



	/**
	 *  Sets the State attribute of the BrowseForm object
	 *
	 *@param  state  The new State value
	 */
	public void setState(String state) {
		this.state = state;
	}


	/**
	 *  Sets the BodyContent attribute of the BrowseForm object
	 *
	 *@param  bodyContent  The new BodyContent value
	 */
	public void setBodyContent(String bodyContent) {
		this.bodyContent = bodyContent;
	}


	/**
	 *  Sets the ContinueBrowsing attribute of the BrowseForm object
	 *
	 *@param  continueBrowsing  The new ContinueBrowsing value
	 */
	public void setContinueBrowsing(String continueBrowsing) {
		this.continueBrowsing = continueBrowsing;
	}


	/**
	 *  Sets the HttpMethod attribute of the BrowseForm object
	 *
	 *@param  httpMethod  The new HttpMethod value
	 */
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}


	/**
	 *  Gets the Hash attribute of the BrowseForm object
	 *
	 *@return    The Hash value
	 */
	public String getLinkHash() {
		return this.linkHash;
	}


	/**
	 *  Gets the From attribute of the BrowseForm object
	 *
	 *@return    The From value
	 */
	public String getFrom() {
		return this.from;
	}


	/**
	 *  Gets the SourceUrl attribute of the BrowseForm object
	 *
	 *@return    The SourceUrl value
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}


	/**
	 *  Gets the State attribute of the BrowseForm object
	 *
	 *@return    The State value
	 */
	public String getState() {
		return state;
	}


	/**
	 *  Gets the BodyContent attribute of the BrowseForm object
	 *
	 *@return    The BodyContent value
	 */
	public String getBodyContent() {
		if (bodyContent == null) {
			setBodyContent("");
		}
		return bodyContent;
	}


	/**
	 *  Gets the ContinueBrowsing attribute of the BrowseForm object
	 *
	 *@return    The ContinueBrowsing value
	 */
	public String getContinueBrowsing() {
		return continueBrowsing;
	}


	/**
	 *  Gets the HttpMethod attribute of the BrowseForm object
	 *
	 *@return    The HttpMethod value
	 */
	public String getHttpMethod() {
		if (httpMethod == null) {
			logger.debug("httpMethod set with a default value");
			setHttpMethod("GET");
		}
		return httpMethod;
	}


	/**
	 *  Gets the Id attribute of the BrowseForm object
	 *
	 *@return    The Id value
	 */
	public int getId() {
		return JahiaClipBuilderConstants.BROWSE;
	}



	/**
	 *  Gets the Errors attribute of the BrowseForm object
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
	 *@param  cBean  ClipperBean parameter
	 */
	public void loadFromClipperBean(ClipperBean cBean) {

	}
}
