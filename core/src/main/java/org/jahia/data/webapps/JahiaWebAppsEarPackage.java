/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
