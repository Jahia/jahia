/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
