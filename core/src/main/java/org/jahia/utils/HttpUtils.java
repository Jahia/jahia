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
//  HttpUtils
//  EV      19.12.2000
//
//  getRemoteFileContents
//

package org.jahia.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.jahia.exceptions.JahiaException;
;



public class HttpUtils {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(HttpUtils.class);

    private static  HttpUtils   theObject = null;



    /***
        * constructor
        * EV    19.12.2000
        *
        */
    private HttpUtils()
    {
        JahiaConsole.println( "Utils", "***** Starting HttpUtils *****" );
    } // end constructor



    /***
        * getInstance
        * EV    19.12.2000
        *
        */
    public static HttpUtils getInstance()
    {
        if (theObject == null) {
            theObject = new HttpUtils();
        }
        return theObject;
    } // end getInstance



    /***
        * getRemoteFileContents
        * EV    03.12.2000
        * called by NewsFeedServices.getNewsFeed(), DataSourceServices
        *
        */
    public String getRemoteFileContents( String urlPath )
    {
        try {
            URL url             = new URL( urlPath );
            InputStreamReader input = new InputStreamReader( (InputStream) url.openStream() );
            BufferedReader in = new BufferedReader( (Reader) input );
            String contents = "";
            String buffer = "";
            while ((buffer = in.readLine()) != null) {
                contents += buffer + "\n";
            }
            return contents;
        } catch (IOException ie) {
            String errorMsg = "Error in reading newsfeed " + urlPath + " : " + ie.getMessage();
            JahiaConsole.println( "NewsFeedManager", errorMsg + " -> Error !" );
            JahiaException je = new JahiaException(   "Cannot access to newsfeed",
                                                errorMsg, JahiaException.FILE_ERROR, JahiaException.ERROR_SEVERITY, ie );
            logger.error("Error:", je);
            return "- No NewsFeed ! -";
        }
    } // end getRemoteFileContents



} // end HttpUtils
