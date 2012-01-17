/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
