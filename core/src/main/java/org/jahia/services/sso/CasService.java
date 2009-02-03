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

 package org.jahia.services.sso;

import edu.yale.its.tp.cas.client.CASAuthenticationException;
import edu.yale.its.tp.cas.client.ServiceTicketValidator;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * <p>Title: CAS service</p>
 * <p>Description: a CAS service to validate CAS tickets.</p>
 * <p>Copyright: Copyright (c) 2005 - Pascal Aubry</p>
 * <p>Company: University of Rennes 1</p>
 * @author Pascal Aubry
 * @version 1.0
 */

public class CasService extends JahiaService {

    /** a logger for this class. */
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger (CasService.class);

    /** a singleton to use a single instance. */
    private static CasService m_Instance;

    /** the name of the configuration file. */
    private static String CONFIGURATION_FILE = "cas.properties";

    /** the property that gives the URL to use to validate tickets. */
    private static String SERVER_VALIDATE_URL_PROP = "cas.server.validateUrl";
    /** the property that gives the service URL of the application. */
    private static String JAHIA_SERVICE_URL_PROP = "cas.jahia.serviceUrl";
    /** the property that gives the login URL of the CAS server. */
    private static String SERVER_LOGIN_URL_PROP = "cas.server.loginUrl";

    /** CAS properties. */
    private Properties casProperties = null;

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

    /** The name of the configuration file. */
    private String configFileName;

    public void start() throws JahiaInitializationException {
        configFileName = settingsBean.getJahiaCasDiskPath() + File.separator + CONFIGURATION_FILE;

        File configFile = new File (configFileName);
        if (configFile.exists()) {
            try {
                File casPropFile = new File (configFileName);
                FileInputStream casPropInputStr = new FileInputStream (casPropFile);
                casProperties = new Properties ();
                casProperties.load (casPropInputStr);
                casPropInputStr.close ();
            } catch (FileNotFoundException fnfe) {
                logger.error(fnfe);
                throw new JahiaInitializationException(fnfe.getMessage(), fnfe);
            } catch (IOException ioe) {
                logger.error(ioe);
                throw new JahiaInitializationException(ioe.getMessage(), ioe);
            }
        } else {
            logger.error("Config file '" + configFileName + "' not found!");
        }
    }

    public void stop() {
    }

    /** constructor. */
    protected CasService() {
        super();
    }

    /**
     * Return a property (throw an exception when not set).
     * @param propName the name of the property
     * @return a String.
     * @throws JahiaInitializationException
     */
    private String getCasProperty(String propName) throws JahiaInitializationException {
        if (casProperties == null) {
            throw new JahiaInitializationException("no CAS property found, please check that '" + configFileName + "' exists!");
        }
        String prop = casProperties.getProperty(propName);
        if (prop == null || "".equals(prop)) {
            throw new JahiaInitializationException("Property '" + propName + "' is not set!");
        }
        return prop;
    }

    /**
     * @return the URL to use to validate tickets.
     * @throws JahiaInitializationException
     */
    public String getServerValidateUrl() throws JahiaInitializationException{
        return getCasProperty(SERVER_VALIDATE_URL_PROP);
    }

    /**
     * @return the login URL of the CAS server.
     * @throws JahiaInitializationException
     */
    public String getServerLoginUrl() throws JahiaInitializationException{
        return getCasProperty(SERVER_LOGIN_URL_PROP);
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

        String validateUrl = getServerValidateUrl();
        logger.debug(SERVER_VALIDATE_URL_PROP + " = " + validateUrl);
        logger.debug(JAHIA_SERVICE_URL_PROP + " = " + serviceUrl);

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

}
