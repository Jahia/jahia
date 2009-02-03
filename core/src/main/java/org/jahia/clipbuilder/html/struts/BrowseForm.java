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
