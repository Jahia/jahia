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
// $Id: Urlmapping.java 26938 2010-03-08 08:36:25Z wassek $
//
//  LicenseInfo
//
//  09.08.2001  MJ  added in jahia.
//
package org.jahia.admin.urlmapping;

import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.JahiaAdministration;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * desc: This class provides the business methods for urlmapping info display,
 * from the JahiaAdministration servlet.
 * 
 * Copyright: Copyright (c) 2010 Company: Jahia Ltd
 * 
 * @author Werner Assek
 * @version 1.0
 */
public class Urlmapping extends AbstractAdministrationModule {
	/*
	 * private static org.slf4j.Logger logger =
	 * org.slf4j.LoggerFactory.getLogger(Urlmapping.class);
	 */

	private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

	/**
	 * Default constructor.
	 * 
	 * @param request
	 *          Servlet request.
	 * @param response
	 *          Servlet response.
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
    throws Exception {
        request.setAttribute("site", request.getSession().getAttribute(ProcessingContext.SESSION_SITE));	
		displayUrlmappings(request, response, request.getSession());
	} // end constructor

	/**
	 * Display urlmappings, using doRedirect().
	 * 
	 * @param req
	 *          the HttpServletRequest object
	 * @param res
	 *          the HttpServletResponse object
	 * @param sess
	 *          the HttpSession object
	 */
	private void displayUrlmappings(HttpServletRequest req,
			HttpServletResponse res, HttpSession sess) throws IOException,
			ServletException {
		JahiaAdministration.doRedirect(req, res, sess, JSP_PATH
				+ "displayurlmappings.jsp");
		// logger.debug("displayurlmappings.jsp called");
	}

}
