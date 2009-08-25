/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.sso;

import edu.yale.its.tp.cas.client.CASAuthenticationException;
import edu.yale.its.tp.cas.client.ServiceTicketValidator;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * <p>Title: CAS service</p>
 * <p>Description: a CAS service to validate CAS tickets.</p>
 * <p>Copyright: Copyright (c) 2005 - Pascal Aubry</p>
 * <p>Company: University of Rennes 1</p>
 * @author Pascal Aubry
 * @version 1.0
 */

public class CasService extends JahiaService {

    /** a singleton to use a single instance. */
    private static CasService m_Instance;

    private String validateUrl;

    private String loginUrl;

    /**
     * return the singleton instance.
     * @return a CasService instance.
     */
    public static synchronized CasService getInstance () {

        if (m_Instance == null) {
            m_Instance = new CasService ();
        }

        return m_Instance;
    }

    public void start() throws JahiaInitializationException {
    	// do nothing
    }

    public void stop() {
    	// do nothing
    }

    /**
     * @return the URL to use to validate tickets.
     * @throws JahiaInitializationException
     */
    public String getValidateUrl() throws JahiaInitializationException{
        return validateUrl;
    }

    /**
     * @return the login URL of the CAS server.
     * @throws JahiaInitializationException
     */
    public String getLoginUrl() throws JahiaInitializationException{
        return loginUrl;
    }

    /**
     * Validate a CAS ticket.
     * @param ticket the ticket
     * @param serviceUrl
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws CASAuthenticationException
     * @throws JahiaInitializationException
     * @return the id of the authenticated user, or null if none.
     */
    public String validateTicket(String ticket, ProcessingContext paramBean, String serviceUrl)
    throws IOException, SAXException, ParserConfigurationException, CASAuthenticationException, MalformedURLException, JahiaException {

        // create a new ticket validator
        ServiceTicketValidator sv = new ServiceTicketValidator();

        // set the URL used to validate the CAS ticket
        sv.setCasValidateUrl(validateUrl);
        // set the service the browser will be redirected after authentication
        sv.setService(serviceUrl);
        // set the ticket to validate
        sv.setServiceTicket(ticket);

        // validate the ticket
        sv.validate();

        if (!sv.isAuthenticationSuccesful()) {
            throw new CASAuthenticationException("error #" + sv.getErrorCode() + " while validating ticket '" + ticket + "': " + sv.getErrorMessage());
        }

        paramBean.getSessionState().setAttribute("cas.pgtiou",sv.getPgtIou());

        return sv.getUser();

    }

	public void setValidateUrl(String serverValidateUrl) {
    	this.validateUrl = serverValidateUrl;
    }

	public void setLoginUrl(String serverLoginUrl) {
    	this.loginUrl = serverLoginUrl;
    }

}
