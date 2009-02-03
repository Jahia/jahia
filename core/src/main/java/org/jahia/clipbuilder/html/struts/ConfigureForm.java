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
import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.util.*;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class ConfigureForm extends ActionForm implements ConfigureInterface {
	private String proxy;
	private String enableSSL;
	private String enableJavascript;
	private String client;
	private String htmlDocument;
	private String enableCSS;
	private String browserJavascriptEvent;
	private String browserJavascriptCode;
	private String portletEnableSSL;
	private String portletContinualClipping;
	private String portletCacheExpiration;
	private String portletCacheContext;


	/**
	 *  Sets the HtmlDocument attribute of the ConfigureForm object
	 *
	 *@param  htmlDocument  The new HtmlDocument value
	 */
	public void setHtmlDocument(String htmlDocument) {
		this.htmlDocument = htmlDocument;
	}


	/**
	 *  Sets the Proxy attribute of the ConfigureForm object
	 *
	 *@param  proxy  The new Proxy value
	 */
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}


	/**
	 *  Sets the Client attribute of the ConfigureForm object
	 *
	 *@param  client  The new Client value
	 */
	public void setClient(String client) {
		this.client = client;
	}


	/**
	 *  Sets the EnableSSL attribute of the ConfigureForm object
	 *
	 *@param  enableSSL  The new EnableSSL value
	 */
	public void setEnableSSL(String enableSSL) {
		this.enableSSL = enableSSL;
	}


	/**
	 *  Sets the EnableJavascript attribute of the ConfigureForm object
	 *
	 *@param  enableJavascript  The new EnableJavascript value
	 */
	public void setEnableJavascript(String enableJavascript) {
		this.enableJavascript = enableJavascript;
	}


	/**
	 *  Sets the EnableCSS attribute of the ConfigureForm object
	 *
	 *@param  enableCSS  The new EnableCSS value
	 */
	public void setEnableCSS(String enableCSS) {
		this.enableCSS = enableCSS;
	}


	/**
	 *  Sets the BrowserJavascriptCode attribute of the ConfigureForm object
	 *
	 *@param  browserJavascriptCode  The new BrowserJavascriptCode value
	 */
	public void setBrowserJavascriptCode(String browserJavascriptCode) {
		this.browserJavascriptCode = browserJavascriptCode;
	}


	/**
	 *  Sets the BrowserJavascriptEvent attribute of the ConfigureForm object
	 *
	 *@param  browserJavascriptEvent  The new BrowserJavascriptEvent value
	 */
	public void setBrowserJavascriptEvent(String browserJavascriptEvent) {
		this.browserJavascriptEvent = browserJavascriptEvent;
	}


	/**
	 *  Sets the PortletContinualClipping attribute of the ConfigureForm object
	 *
	 *@param  portletContinualClipping  The new PortletContinualClipping value
	 */
	public void setPortletContinualClipping(String portletContinualClipping) {
		this.portletContinualClipping = portletContinualClipping;
	}


	/**
	 *  Sets the PortletEnableSSL attribute of the ConfigureForm object
	 *
	 *@param  portletEnableSSL  The new PortletEnableSSL value
	 */
	public void setPortletEnableSSL(String portletEnableSSL) {
		this.portletEnableSSL = portletEnableSSL;
	}


	/**
	 *  Sets the PortletCacheExpiration attribute of the ConfigureForm object
	 *
	 *@param  portletCacheExpiration  The new PortletCacheExpiration value
	 */
	public void setPortletCacheExpiration(String portletCacheExpiration) {
		this.portletCacheExpiration = portletCacheExpiration;
	}


	/**
	 *  Sets the PortletCacheContext attribute of the ConfigureForm object
	 *
	 *@param  portletCacheContext  The new PortletCacheContext value
	 */
	public void setPortletCacheContext(String portletCacheContext) {
		this.portletCacheContext = portletCacheContext;
	}


	/**
	 *  Gets the Proxy attribute of the ConfigureForm object
	 *
	 *@return    The Proxy value
	 */
	public String getProxy() {
		return proxy;
	}


	/**
	 *  Gets the Client attribute of the ConfigureForm object
	 *
	 *@return    The Client value
	 */
	public String getClient() {
		return client;
	}


	/**
	 *  Gets the EnableSSL attribute of the ConfigureForm object
	 *
	 *@return    The EnableSSL value
	 */
	public String getEnableSSL() {
		return enableSSL;
	}


	/**
	 *  Gets the EnableJavascript attribute of the ConfigureForm object
	 *
	 *@return    The EnableJavascript value
	 */
	public String getEnableJavascript() {
		return enableJavascript;
	}


	/**
	 *  Gets the HtmlDocument attribute of the ConfigureForm object
	 *
	 *@return    The HtmlDocument value
	 */
	public String getHtmlDocument() {
		return htmlDocument;
	}


	/**
	 *  Gets the EnableCSS attribute of the ConfigureForm object
	 *
	 *@return    The EnableCSS value
	 */
	public String getEnableCSS() {
		return enableCSS;
	}


	/**
	 *  Gets the BrowserJavascriptCode attribute of the ConfigureForm object
	 *
	 *@return    The BrowserJavascriptCode value
	 */
	public String getBrowserJavascriptCode() {
		return browserJavascriptCode;
	}


	/**
	 *  Gets the BrowserJavascriptEvent attribute of the ConfigureForm object
	 *
	 *@return    The BrowserJavascriptEvent value
	 */
	public String getBrowserJavascriptEvent() {
		return browserJavascriptEvent;
	}


	/**
	 *  Gets the PortletEnableSSL attribute of the ConfigureForm object
	 *
	 *@return    The PortletEnableSSL value
	 */
	public String getPortletEnableSSL() {
		return portletEnableSSL;
	}


	/**
	 *  Gets the PortletContinualClipping attribute of the ConfigureForm object
	 *
	 *@return    The PortletContinualClipping value
	 */
	public String getPortletContinualClipping() {
		return portletContinualClipping;
	}


	/**
	 *  Gets the PortletCacheExpiration attribute of the ConfigureForm object
	 *
	 *@return    The PortletCacheExpiration value
	 */
	public String getPortletCacheExpiration() {
		return portletCacheExpiration;
	}


	/**
	 *  Gets the PortletCacheContext attribute of the ConfigureForm object
	 *
	 *@return    The PortletCacheContext value
	 */
	public String getPortletCacheContext() {
		return portletCacheContext;
	}


	/**
	 *  Sets the AllPropertiesToNull attribute of the ConfigureForm object
	 *
	 *@param  configuration  Description of Parameter
	 */
	public void load(ConfigureInterface configuration) {
		ClassUtilities.synchronize(configuration, this);
	}

}
