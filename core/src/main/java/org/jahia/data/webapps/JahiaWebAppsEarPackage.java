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
//
//  JahiaWebAppsEarPackage
//
//  NK      16.01.2001
//
//

package org.jahia.data.webapps;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds Informations about a webapps ear package
 *
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaWebAppsEarPackage {

   	/** The List of JahiaWebAppDef object *
   	 * @associates JahiaWebAppDef*/
   	private List<JahiaWebAppDef> m_WebApps = new ArrayList<JahiaWebAppDef>();       

	/** the application context in case of ear file **/
	private String m_ApplicationContextRoot;

   
   /**
    * Constructor
    * 
    * @param (String) context, the application context  in case of ear file
    */
	public JahiaWebAppsEarPackage ( String context ) {
	   m_ApplicationContextRoot = context;
	}


   /**
    * Get the list of JahiaWebAppDef object
    *
    * @return (List) the List of JahiaWebAppDef object
    */
   public List<JahiaWebAppDef> getWebApps(){
      
      return m_WebApps;
   
   } 

   /**
    * Add a WebApps Definition in the Web Apps list
    *
    * @param (JahiaWebAppDef) webAppDef
    */
   public void addWebAppDef(JahiaWebAppDef webAppDef ){
      
      m_WebApps.add(webAppDef);
   
   } 

   /**
    * Add a List of JahiaWebAppDef object at the end of the WebApps List
    *
    * @param (List) the List
    */
   public void addWebAppDefs(List<JahiaWebAppDef> vec){
      
      m_WebApps.addAll(vec);
   
   } 

   
   /**
    * Returns the Context Root of this application
    *
    * @return (String) the context root
    */    
   public String getContextRoot(){
      
      return m_ApplicationContextRoot;
      
   }
    

   /**
    * Set the Application Context
    *
    * @param (String) the application context
    */    
   public void setContextRoot(String context){
      
      m_ApplicationContextRoot = context;
      
   }
    
    
} // end JahiaWebAppEarPackage
