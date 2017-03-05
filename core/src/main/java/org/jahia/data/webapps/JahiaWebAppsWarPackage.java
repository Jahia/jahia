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
