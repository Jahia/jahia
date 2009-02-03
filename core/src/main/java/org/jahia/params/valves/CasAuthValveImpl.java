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

 package org.jahia.params.valves;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.valves.SsoValve;
import org.jahia.services.sso.CasService;

/**
 * <p>Title: CAS valve</p>
 * <p>Description: authenticate users with a CAS server.</p>
 * <p>Copyright: Copyright (c) 2005 - Pascal Aubry</p>
 * <p>Company: University of Rennes 1</p>
 * @author Pascal Aubry
 * @version 1.0
 */

public class CasAuthValveImpl extends SsoValve {


    /** constructor. */
    public CasAuthValveImpl () {
		// nothing to do here
    }

	/**
	 * @see org.jahia.pipelines.valves.Valve#initialize()
	 */
	public void initialize() {
		// nothing to do here
	}

    /**
     * @throws org.jahia.exceptions.JahiaException
     */
    public String validateCredentials(Object credentials, ProcessingContext paramBean)
        throws JahiaException {
        try {
            return CasService.getInstance().validateTicket((String) credentials, paramBean, getServiceUrl(paramBean));
        } catch (Exception e) {
            throw new JahiaException("Cannot validate CAS credentials", "Cannot validate CAS credentials", JahiaException.SECURITY_ERROR, JahiaException.WARNING_SEVERITY,e);
        }
    }


    /**
     */
    public Object retrieveCredentials(ProcessingContext processingContext) throws Exception {
        String ticket = ((ParamBean)processingContext).getRequest().getParameter("ticket");
        if (ticket == null) {
            return null;
        }
        if (ticket.equals("")) {
            return null;
        }
        return ticket;
    }

    /**
     * @throws JahiaInitializationException
     */
    public String getRedirectUrl(ProcessingContext processingContext) throws JahiaException {
        CasService casService = CasService.getInstance();

        String redirectUrl = getServiceUrl(processingContext);

        return casService.getServerLoginUrl() + "?service=" + redirectUrl;
    }

    private String getServiceUrl(ProcessingContext processingContext) {
        int pid = processingContext.getPageID();
        if (pid == -1) {
            logger.warn("pid is -1");
            String spid = processingContext.getParameter("pid");
            try {
                pid = Integer.parseInt(spid);
                logger.debug("pid parameter = "+pid);
            } catch (NumberFormatException e) {
            }
            logger.debug("contentpage = "+processingContext.getContentPage());
            logger.debug("homecontentpage = "+processingContext.getSite().getHomeContentPage());
        }

        logger.debug("get serviceUrl, pid = "+pid );
        final String siteServerName = processingContext.getSite().getServerName();

        final StringBuffer redirectUrl = new StringBuffer(processingContext.getScheme() + "://");
        redirectUrl.append(siteServerName);

        if (processingContext.getServerPort() != 80) {
            redirectUrl.append(":");
            redirectUrl.append(processingContext.getServerPort());
        }

        redirectUrl.append(processingContext.getContextPath());
        redirectUrl.append(Jahia.getServletPath());

        if (pid != -1) {
            String pageURLPart = processingContext.getPageURLPart(pid);
            logger.debug("pageURLpart = "+pageURLPart );
            redirectUrl.append(pageURLPart);
        }
        return redirectUrl.toString();
    }

}
