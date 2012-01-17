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
//  JahiaWebAppsPackage
//
//  NK      16.01.2001
//
//

package org.jahia.data.webapps;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds Informations about a webapps package ( a war , ear or even an unziped directory
 *
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaWebAppsPackage {

   	/**
   	 * A list of JahiaWebAppDef Object
   	 * @associates JahiaWebAppDef
   	 */
   	private List<JahiaWebAppDef> m_WebApps = new ArrayList<JahiaWebAppDef>();

   	/**
   	 * The ContextRoot for all the web apps within the package
   	 * in case of ear file, it's the application context
   	 **/
   	private String m_ContextRoot ;

   	/** the file or directory name from which data are loaded **/
   	private String m_FileName;

   	/** the full path to the source file or directory **/
   	private String m_FilePath;

   	/** the package type **/
   	private int m_Type ;	// 1=war, 2=ear, 3=directory

	/** war package **/
	public static final int WAR = 1;

	/** ear package **/
	public static final int EAR = 2;

	/** directory **/
	public static final int DIR = 3;

	/** has EJB or not ? **/
	private boolean m_HasEJB = false;


   /**
    * Constructor
    *
    * @param (String) contextRoot , the context root of the web apps
    */
	public JahiaWebAppsPackage ( String contextRoot ) {
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
    * Set the WebApps List
    *
    * @param (List) the List of webapps list
    */
   public void addWebAppDef(List<JahiaWebAppDef> vec){

      m_WebApps.addAll(vec);

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


	/**
	 * get the source filename
	 *
	 */
    public String getFileName(){
    	return this.m_FileName;
    }


	/**
	 * set the source filename
	 *
	 */
    public void setFileName(String name){
    	this.m_FileName = name;

		if ( name.endsWith(".war") ){
			m_Type = WAR;
		} else if ( name.endsWith(".ear") ){
			m_Type = EAR;
		} else  {
			m_Type = DIR;
		}
    }


	/**
	 * get the file path
	 *
	 */
    public String getFilePath(){
    	return this.m_FilePath;
    }

	/**
	 * set the file path
	 *
	 */
    public void setFilePath(String path){
    	this.m_FilePath = path;
    }



	/**
	 * if the source is a war file
	 *
	 */
    public boolean isWarFile(){
        return (m_Type == WAR);
    }


	/**
	 * if the source is an ear file
	 *
	 */
    public boolean isEarFile(){
        return ( m_Type == EAR );
    }


	/**
	 * if the source is a directory
	 *
	 */
    public boolean isDirectory(){
        return ( m_Type == DIR );
    }


	/**
	 * set has EJB or not
	 */
	public void setHasEJB(boolean val){
		m_HasEJB = val;
	}


	/**
	 * uses EJB or not ?
	 * @return boolean has EJB or not
	 */
    public boolean hasEJB(){
    	return m_HasEJB;
    }

    public void setType(int m_Type) {
        this.m_Type = m_Type;
    }

} // end JahiaWebAppsPackage
