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
//  JahiaWebAppsWarPackage
//
//  NK      16.01.2001
//
//

package org.jahia.data.webapps;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds Informations about a webapps war package
 *
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaWebAppsWarPackage {

   /** The WebApps list *
    * @associates JahiaWebAppDef*/
   private List<JahiaWebAppDef> m_WebApps = new ArrayList<JahiaWebAppDef>();       

   /** The ContextRoot for all the web apps within the war file **/
   private String m_ContextRoot ;
   
   /**
    * Constructor
    * 
    * @param (String) contextRoot , the context root of the web apps
    */
	public JahiaWebAppsWarPackage ( String contextRoot ) {
	   m_ContextRoot = contextRoot;
	}
    
   /**
    * Get the WebApps List 
    *
    * @return (List) the List of webapps list
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
    * Returns the Context Root of this package
    *
    * @return (String) the context root
    */    
   public String getContextRoot(){
      
      return m_ContextRoot;
      
   }
    
    
    
} // end JahiaWebAppWarPackage
