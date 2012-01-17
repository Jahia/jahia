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
//  Security_Role_Ref
//
//  NK      29.01.2001
//
//

package org.jahia.data.webapps;




/**
 * Holds Informations about the <web> components element in the descriptors file
 * web.xml
 *
 *       <servlet>
 *          <servlet-name>FilemanagerServlet</servlet-name>
 *          <display-name>Filemanager Portlet</display-name>
 *          <desc>no desc</desc>
 *          <servlet-class>org.jahia.portlets.filemanager.FilemanagerServlet</servlet-class>
 *          <init-param>
 *             <param-name>properties</param-name>
 *             <param-value>WEB-INF\conf\config.properties</param-value>
 *          </init-param>
 *
 *
 *	-->		<security-role_ref>
 *				<desc></desc>
 *				<role-name>Editor</role-name>
 *				<role-link>Editor</role-link>
 *			</security-role-ref>
 *
 *
 *       </servlet>
 *
 *
 * @author Khue ng
 * @version 1.0
 */
public class Security_Role_Ref {

   /** The role name  **/
   private String m_Name;
   /** The role link **/
   private String m_Link;
   /** The desc **/
   private String m_Descr;

   /**
    * Constructor
    *
    */
    public Security_Role_Ref(	String name,
                          		String link,
                        		String descr
                           	){

       m_Name = name;
       m_Link = link;
       m_Descr= descr;

    }


   /**
    * Return the role name
    *
    * @return (String) the role name
    */
   public String getName(){

      return m_Name;
   }


   /**
    * Return the role link
    *
    * @return (String) the role link
    */
   public String getLink(){

      return m_Link;
   }


   /**
    * Return the role descr
    *
    * @return (String) the role descr
    */
   public String getDescr(){

      return m_Descr;
   }


} // end Web_Component
