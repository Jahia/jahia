/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
