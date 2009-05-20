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
//  JahiaDataSourceManager
//  EV      03.12.2000
//
//  fields()
//

package org.jahia.sharing;

import org.jahia.utils.JahiaConsole;



public class JahiaDataSourceManager {

    private static  JahiaDataSourceManager  theObject       = null;
    private         FieldSharingManager     fields          = null;

	

    /***
        * constructor
	    * EV    03.12.2000
        *
        */
	private JahiaDataSourceManager()
	{
	    JahiaConsole.println( "JahiaDataSourceManager", "***** Starting the Jahia SearchIndexer Manager *****" );
	    fields = FieldSharingManager.getInstance();
	} // end constructor
	
	

    /***
        * getInstance
	    * EV    03.12.2000
        *
        */
	public static synchronized JahiaDataSourceManager getInstance()
	{
	    if (theObject == null) {
	        theObject = new JahiaDataSourceManager();
	    }
	    return theObject;
	} // end getInstance
	


    /***
        * fields
	    * EV    03.12.2000
        *
        */
	public FieldSharingManager fields()
	{
	    return fields;
	} // end fields


} // end JahiaDataSourceManager
