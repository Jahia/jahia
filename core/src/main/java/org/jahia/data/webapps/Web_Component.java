/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//
//  Web_Component
//
//  NK      29.01.2001
//
//

package org.jahia.data.webapps;




/**
 * Holds Informations about the <web> components element in the descriptors file
 * application.xml ( J2EE Standard )
 *
 * <application>
 *    <display-name>filemanager.ear</display-name>
 *    <desc>Application desc</desc>
 *    <module>
 *
 *       <web>
 *          <web-uri>war-ic.war</web-uri>
 *          <context-root>filemanager</context-root>
 *       </web>
 *
 *    </module>
 * </application>
 *
 *
 * @author Khue ng
 * @version 1.0
 */
public class Web_Component {

   /** The web-uri  **/
   private String m_WebURI;
   /** The context-root **/
   private String m_ContextRoot;

   /**
    * Constructor
    *
    * @param (String) path, the full path to the application.xml file
    */
    public Web_Component( String webURI,
                          String contextRoot
                        ){

       m_WebURI = webURI;
       m_ContextRoot = contextRoot;

    }


   /**
    * Return the webURI
    *
    * @return (String) the web URI
    */
   public String getWebURI(){

      return m_WebURI;
   }


   /**
    * Return the Context Root
    *
    * @return (String) the Context Root
    */
   public String getContextRoot(){

      return m_ContextRoot;
   }


} // end Web_Component
