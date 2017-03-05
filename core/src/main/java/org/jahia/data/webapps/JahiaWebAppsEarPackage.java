/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
