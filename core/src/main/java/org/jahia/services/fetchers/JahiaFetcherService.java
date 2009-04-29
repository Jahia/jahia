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
//
//  JahiaFetcherServices
//  EV      11.01.2001
//
//
//  fetchServlet( jParams, servletPath )
//

package org.jahia.services.fetchers;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.JahiaService;

/**
  * Class JahiaFetcherService
  *
  */
public abstract class JahiaFetcherService extends JahiaService {



    /***
        * fetches a servlet or JSP output
        *
        * @param        jParams             a ProcessingContext object (with request and response)
        * @param        servletPath         the servlet / jsp context path
        * @return       the servlet / jsp output
        *
        */
	public abstract String fetchServlet( ParamBean jParams, String servletPath )
	    throws JahiaException;


} // end JahiaFetcherService
