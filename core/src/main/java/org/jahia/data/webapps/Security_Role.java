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
//  Security_Role
//
//  NK      29.01.2001
//
//

package org.jahia.data.webapps;




/**
 * Holds Informations about the <security-role> element in the descriptors file
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
 *      	</init-param>
 *      </servlet>
 *
 * -->	<security-role>
 *			<desc></desc>
 *			<role-name>Editor</role-name>
 *		</security-role>
 *
 * @author Khue ng
 * @version 1.0
 */
public class Security_Role {

   /** The role name  **/
   private String m_Name;
   /** The desc **/
   private String m_Descr;

   /**
    * Constructor
    *
    */
    public Security_Role	(	String name,
                        		String descr
                           	){

       m_Name = name;
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
    * Return the role descr
    *
    * @return (String) the role descr
    */
   public String getDescr(){

      return m_Descr;
   }


} // end Web_Component
