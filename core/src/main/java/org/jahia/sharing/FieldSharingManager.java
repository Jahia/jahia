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
//  FieldSharingManager
//  EV      03.12.2000
//
//  getRemoteFieldValue( urlPath )
//

package org.jahia.sharing;

import org.jahia.utils.JahiaConsole;


public class FieldSharingManager {

    private static  FieldSharingManager theObject       = null;



    /***
        * constructor
        * EV    03.12.2000
        *
        */
    private FieldSharingManager()
    {
        JahiaConsole.println( "FieldSharingManager", "***** Starting the FieldSharing Manager *****" );
    } // end constructor



    /***
        * getInstance
        * EV    03.12.2000
        *
        */
    public static synchronized FieldSharingManager getInstance()
    {
        if (theObject == null) {
            theObject = new FieldSharingManager();
        }
        return theObject;
    } // end getInstance



    /***
        * getRemoteFieldValue
        * EV    03.12.2000
        * called by SelectDataSource_Engine.processForm
        *
        */
    public String getRemoteFieldValue( String jahiaPath )
    {
        // FOR DEMO PURPOSES ONLY
        // should :
        //  - connect to remote jahia server through specific port
        //  - parse data source in XML (soap ?)
        //  - conquer the world
        String contents = "Not implemented yet.";
        return contents;
    } // end getRemoteFieldValue



} // end FieldSharingManager
