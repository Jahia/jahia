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
