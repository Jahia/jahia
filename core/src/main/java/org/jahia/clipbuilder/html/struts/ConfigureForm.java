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
