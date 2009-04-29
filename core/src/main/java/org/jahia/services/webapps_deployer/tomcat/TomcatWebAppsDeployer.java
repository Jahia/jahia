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
//  NK      06.02.2001
//
//


package org.jahia.services.webapps_deployer.tomcat;


import org.jahia.data.constants.JahiaConstants;
import org.jahia.utils.JahiaTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Handle web app deployment and activation under tomcat
 * Create a Url object handler used to call the Tomcat Management
 * Application servlet Url ( org.apache.catalina.servlets.ManagerServlet ).
 * We call this servlet to deploy webapps under tomcat
 *
 * @author Khue ng
 * @version 1.0
 */


public class TomcatWebAppsDeployer {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (TomcatWebAppsDeployer.class);

    /** The Tomcat Version * */
    private static String m_TomcatVersion = JahiaConstants.SERVER_TOMCAT;

    /** The Tomcat user name * */
    private static String m_TomcatUserName = "Jahia";

    /** The Tomcat user password * */
    private static String m_TomcatUserPassword = "Jahia";

    /** the Server Host Http Path * */
    private String m_JahiaWebAppsDeployerBaseURL = "";

    /** the list url * */
    private String m_ListUrlBase = "";

    /** the deploy url * */
    private String m_DeployUrlBase = "";

    /** the undeploy url * */
    private String m_UnDeployUrlBase = "";

    /** the reload url * */
    private String m_ReloadUrlBase = "";

    /** the start url * */
    private String m_StartUrlBase = "";

    /** the stop url * */
    private String m_StopUrlBase = "";

    /** The Url Connection * */
    private URLConnection m_Conn = null;

    private URL m_ContextURL = null;

    /**
     * Constructor
     *
     * @param tomcatVersion the version of Tomcat used with Jahia
     * @param m_WebAppsDeployerBaseURL the URL to Tomcat's manager web application
     * @param username a Tomcat user that has "manager" rights access
     * @param password a Tomcat user password for the "manager" user.
     */
    public TomcatWebAppsDeployer (String tomcatVersion,
                                  String m_WebAppsDeployerBaseURL,
                                  String username,
                                  String password
                                  ) {


        m_TomcatVersion = tomcatVersion;
        m_JahiaWebAppsDeployerBaseURL = m_WebAppsDeployerBaseURL;
        m_TomcatUserName = username;
        m_TomcatUserPassword = password;

        try {
            m_ContextURL = new URL ("http://localhost:8080");
        } catch (MalformedURLException mue) {
            logger.error ("Error in URL http://localhost:8080", mue);
        }

        /*
        logger.debug("TomcatWebAppsDeployer, using username=" +
                             m_TomcatUserName + " and password=" +
                             m_TomcatUserPassword);
        */

        //default
        m_ListUrlBase = m_JahiaWebAppsDeployerBaseURL + "/list?";
        m_DeployUrlBase = m_JahiaWebAppsDeployerBaseURL + "/deploy?";
        m_UnDeployUrlBase = m_JahiaWebAppsDeployerBaseURL + "/undeploy?";
        m_ReloadUrlBase = m_JahiaWebAppsDeployerBaseURL + "/reload?";
        m_StartUrlBase = m_JahiaWebAppsDeployerBaseURL + "/start?";
        m_StopUrlBase = m_JahiaWebAppsDeployerBaseURL + "/stop?";

        if (m_TomcatVersion.endsWith (JahiaConstants.SERVER_TOMCAT4_BETA1)) { // the server is tomcatb1...
            m_DeployUrlBase = m_JahiaWebAppsDeployerBaseURL + "/install?";
            m_UnDeployUrlBase = m_JahiaWebAppsDeployerBaseURL + "/remove?";
        }

        m_DeployUrlBase = m_JahiaWebAppsDeployerBaseURL + "/install?";
        m_UnDeployUrlBase = m_JahiaWebAppsDeployerBaseURL + "/remove?";

    }


    //-------------------------------------------------------------------------
    /**
     * Call the list url , below is what we receive in text/plain
     * *	OK - Listed applications for virtual host localhost
     * *	/Bookcards:running:0
     * *	/TodoList:stopped:0
     * *	/examples2:running:0
     * *	/webdav:running:0
     * *	/jahia:running:1
     * *	/examples:running:0
     * *	/manager:running:0
     * *	/:running:0
     *
     * @return (List) return a List of context retrieved from the response
     *         null if fail.
     */
    public List getAppList () {

        logger.debug ("Retrieving application list from server through URL="
                + m_ListUrlBase);

        String outPut = null;
        try {
            outPut = readInputStream (new URL (m_ContextURL, m_ListUrlBase),
                    m_TomcatUserName,
                    m_TomcatUserPassword);
        } catch (java.net.MalformedURLException mfu) {
            logger.error ("Error in URL processing", mfu);
            return null;
        }

        List vec = new ArrayList();

        if (outPut != null && outPut.startsWith ("OK")) {

            String[] tockens = JahiaTools.getTokens (outPut, "/");
            for (int i = 1; i < (tockens.length - 1); i++) {
                vec.add (tockens[i]);
            }
        } else {
            logger.error (
                    "Error while retrieving application list with URL=" + m_ListUrlBase + " output=" + outPut);
            vec = null;
        }
        return vec;
    }



    //-------------------------------------------------------------------------
    /**
     * Call the deploy url
     *
     * @param path the context path to associate to the web apps
     * @param war the path to a war file to deploy or the path to
     *                 the unpacked directory to asscotiate with a context
     *                 ex: http://127.0.0.1:8080/manager/deploy?path=/AbsenceRequest&
     *                 war=jar:file:/d:/tomcat/webapps/jahia/war_webapps/AbsenceRequest.war!/
     *
     * @return (boolean) return true if deployed correctly
     */
    public boolean deploy (String path,
                           String war) {


        StringBuffer buff = new StringBuffer (1024);
        buff.append (m_DeployUrlBase);
        buff.append ("path=");
        buff.append (path);
        buff.append("&update=true");
        buff.append ("&");
        buff.append ("war=");
        buff.append (war);

        String outPut = null;
        URL deployURL = null;

        try {
            deployURL = new URL (m_ContextURL, buff.toString ());

            logger.debug ("deploying via URL=" +
                    deployURL.toString () + "...");

            outPut = readInputStream (deployURL,
                    m_TomcatUserName,
                    m_TomcatUserPassword);

        } catch (java.net.MalformedURLException mfu) {
            logger.error ("Error while deploying", mfu);
            return false;
        }


        if (outPut != null) {
            if (outPut.startsWith ("OK")) {
                logger.debug ("deployment via URL " +
                        deployURL.toString () + " successfull");
                return true;
            } else {
                logger.debug ("deployment via URL " +
                        deployURL.toString () +
                        " not successful. Returned output=" +
                        outPut);
            }
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Call the undeploy url
     *
     * @param path the context path to associate to the web apps
     *                 ex: http://127.0.0.1:8080/manager/undeploy?path=/AbsenceRequest
     *
     * @return return true if deployed correctly
     */
    public boolean undeploy (String path) {


        StringBuffer buff = new StringBuffer (1024);
        buff.append (m_UnDeployUrlBase);
        buff.append("path=").append(path);

        logger.debug ("Undeploying using URL="
                + buff.toString ());

        String outPut = null;
        try {
            outPut = readInputStream (new URL (m_ContextURL, buff.toString ()),
                    m_TomcatUserName,
                    m_TomcatUserPassword);
        } catch (java.net.MalformedURLException mfu) {
            logger.error ("Error while undeploying", mfu);
            return false;
        }


        if (outPut != null && outPut.startsWith ("OK")) {
            logger.debug ("Undeploy successful");
            return true;
        } else {
            logger.debug ("Undeployment via URL " +
                    buff.toString () +
                    " not successful. Returned output=" +
                    outPut);
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Call the stop url
     *
     * @param path the context path to associate to the web apps
     *                 ex: http://127.0.0.1:8080/manager/stop?path=/AbsenceRequest
     *
     * @return true if stopped correctly
     */
    public boolean stop (String path) {


        StringBuffer buff = new StringBuffer (1024);
        buff.append (m_StopUrlBase);
        buff.append("path=").append(path);

        logger.debug ("Stopping application using URL="
                + buff.toString ());

        String outPut = null;
        try {

            outPut = readInputStream (new URL (m_ContextURL, buff.toString ()),
                    m_TomcatUserName,
                    m_TomcatUserPassword);
        } catch (java.net.MalformedURLException mfu) {
            logger.error ("Error in stop URL", mfu);
            return false;
        }

        if (outPut != null && outPut.startsWith ("OK")) {
            logger.debug ("Stop successful");
            return true;
        } else {
            logger.error (
                    "Stop via URL=" + buff.toString () + " unsuccessful. Output=" + outPut);
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Call the start url
     *
     * @param path the context path to associate to the web apps
     *                 ex: http://127.0.0.1:8080/manager/start?path=/AbsenceRequest
     *
     * @return true if started correctly
     */
    public boolean start (String path) {


        StringBuffer buff = new StringBuffer (1024);
        buff.append (m_StartUrlBase);
        buff.append("path=").append(path);

        logger.debug ("Starting application using URL=" + buff.toString ());

        String outPut = null;
        try {
            outPut = readInputStream (new URL (m_ContextURL, buff.toString ()),
                    m_TomcatUserName,
                    m_TomcatUserPassword);
        } catch (java.net.MalformedURLException mfu) {
            logger.error ("Error while starting application using URL" + buff.toString (), mfu);
            return false;
        }

        if (outPut != null && outPut.startsWith ("OK")) {
            logger.debug ("Application started successfully");
            return true;
        } else {
            logger.error (
                    "Error starting application with URL=" + buff.toString () + ". Output=" + outPut);
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Call the reload url
     *
     * @param path the context path to associate to the web apps
     *                 ex: http://127.0.0.1:8080/manager/reload?path=/AbsenceRequest
     *
     * @return true if reloaded correctly
     */
    public boolean reload (String path) {

        StringBuffer buff = new StringBuffer (1024);
        buff.append (m_ReloadUrlBase);
        buff.append("path=").append(path);

        logger.debug ("Reloading application using URL=" + buff.toString ());

        String outPut = null;
        try {
            outPut = readInputStream (new URL (m_ContextURL, buff.toString ()),
                    m_TomcatUserName,
                    m_TomcatUserPassword);
        } catch (java.net.MalformedURLException mfu) {
            logger.error ("Error while reloading application using URL=" + buff.toString (),
                    mfu);
            return false;
        }

        if (outPut != null && outPut.startsWith ("OK")) {
            logger.debug ("Application successfully reloaded.");
            return true;
        } else {
            logger.error (
                    "Error while reloading application using URL=" + buff.toString () + ". Output=" + outPut);
        }

        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Read input stream from url
     *
     * @param url the url
     * @param username the username
     * @param password the password
     *
     * @return the response in text/plain
     */
    public String readInputStream (URL url,
                                   String username,
                                   String password
                                   ) {

        StringBuffer outPut = new StringBuffer (1024);
        String line = null;

        try {
            BufferedReader in = new BufferedReader (
                    new InputStreamReader (openURLForInput (url, username, password)));
            while ((line = in.readLine ()) != null) {
                outPut.append (line);
            }
        } catch (IOException e) {
            logger.debug ("Error while retrieving content of URL" + url.toString (), e);
            return null;
        }
        return outPut.toString ();
    }


    //-------------------------------------------------------------------------
    /**
     * Open an url connection
     *
     * @param url the url
     * @param username the username
     * @param password the password
     *
     * @return the opened InputStream for the specified URL
     */
    public InputStream openURLForInput (URL url,
                                        String username,
                                        String password
                                        ) throws IOException {
        m_Conn = url.openConnection ();
        m_Conn.setDoInput (true);
        m_Conn.setRequestProperty ("Authorization",
                userNamePasswordBase64 (username, password)
        );
        m_Conn.connect ();
        return m_Conn.getInputStream ();
    }


    //-------------------------------------------------------------------------
    /**
     * generate a base 64 encode user name and password string
     *
     * @param username the username
     * @param password the password
     *
     * @return the base 64 encoded user name and password
     */
    public static String userNamePasswordBase64 (String username, String password) {
        return "Basic " + base64Encode (username + ":" + password);
    }


    private final static char base64Array [] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };


    //-------------------------------------------------------------------------
    private static String base64Encode (String string) {
        String encodedString = "";
        byte bytes [] = string.getBytes ();
        int i = 0;
        int pad = 0;
        while (i < bytes.length) {
            byte b1 = bytes[i++];
            byte b2;
            byte b3;
            if (i >= bytes.length) {
                b2 = 0;
                b3 = 0;
                pad = 2;
            } else {
                b2 = bytes[i++];
                if (i >= bytes.length) {
                    b3 = 0;
                    pad = 1;
                } else
                    b3 = bytes[i++];
            }
            byte c1 = (byte) (b1 >> 2);
            byte c2 = (byte) (((b1 & 0x3) << 4) | (b2 >> 4));
            byte c3 = (byte) (((b2 & 0xf) << 2) | (b3 >> 6));
            byte c4 = (byte) (b3 & 0x3f);
            encodedString += base64Array[c1];
            encodedString += base64Array[c2];
            switch (pad) {
                case 0:
                    encodedString += base64Array[c3];
                    encodedString += base64Array[c4];
                    break;
                case 1:
                    encodedString += base64Array[c3];
                    encodedString += "=";
                    break;
                case 2:
                    encodedString += "==";
                    break;
            }
        }
        return encodedString;
    }


}
