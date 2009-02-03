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
