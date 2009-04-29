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
 package org.jahia.clipbuilder.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.JahiaAdministration;
import org.springframework.context.ApplicationContext;

/**
 *  Jahia utils: make easier jahia inetgration When integreated whith jahia,
 *  uncomment java-code
 *
 *@author    ktlili
 */
public class JahiaUtils {


	/**
	 *  Sets the IntallMenuAttributes attribute of the JahiaUtils class
	 *
	 *@param  request  The new IntallMenuAttributes value
	 */
	public static void setIntallMenuAttributes(HttpServletRequest request,HttpServletResponse response,HttpSession session) {
		request.setAttribute("URL", JahiaAdministration.installerURL);
	}


	/**
	 *  Gets the PortletDiretcoryPath attribute of the JahiaUtils class
	 *
	 *@param  request  Description of Parameter
	 *@return          The PortletDiretcoryPath value
	 */
	public static String getPortletDiretcoryPath(HttpServletRequest request) {
		//un-comment when integrated to jahia
	//	JahiaSite jSite = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
	//	String siteKey = jSite.getSiteKey();
	//	String webAppsDir = siteKey;
		return org.jahia.settings.SettingsBean.getInstance().getJahiaNewWebAppsDiskPath();// + File.separator + webAppsDir;
		//return null;
	}



	/**
	 *  Gets the SpringApplicationContext attribute of the JahiaUtils class
	 *
	 *@return    The SpringApplicationContext value
	 */
	public static ApplicationContext getSpringApplicationContext() {
		//  un-comment when integrated to jahia
		return org.jahia.hibernate.manager.SpringContextSingleton.getInstance().getContext();
		//return null;
	}

}
