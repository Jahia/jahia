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
//  JahiaChrono
//  EV      23.12.2000
//
//  getInstance()
//  start()
//  read( startTime )
//  toConsole( startTime, message )
//

package org.jahia.utils;

import java.util.Date;

public class JahiaChrono
{

    private static JahiaChrono theObject = null;


    /***
        * constructor
        * EV    23.12.2000
        *
        */
    private JahiaChrono()
    {
        JahiaConsole.println( "JahiaChrono", "Starting JahiaChrono utils" );
    } // end constructor



    /***
        * getInstance
        * EV    23.12.2000
        *
        */
    public static JahiaChrono getInstance()
    {
        if (theObject == null) {
            theObject = new JahiaChrono();
        }
        return theObject;
    } // end getInstance



    /***
        * start
        * EV    23.12.2000
        *
        */
    public long start()
    {
        return (new Date()).getTime();
    } // end start



    /***
        * read
        * EV    23.12.2000
        *
        */
    public long read( long startTime )
    {
        return ((new Date()).getTime() - startTime);
    } // end read



    /***
        * toConsole
        * EV    23.12.2000
        *
        */
    public void toConsole( long startTime, String message )
    {
        JahiaConsole.println( "JahiaChrono", "[" + read(startTime) + "ms] " + message );
    } // end toConsole


}