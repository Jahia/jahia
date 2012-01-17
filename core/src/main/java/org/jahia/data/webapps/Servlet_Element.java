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
//  Servlet_Element
//
//  NK      29.01.2001
//
//

package org.jahia.data.webapps;




/**
 * Holds Informations about the <servlet> declaration tag in the descriptors file
 * web.xml
 *
 * <pre>
 *    <web-app>
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
 *       </servlet>
 *
 *    </web-app>
 * </pre>
 *
 * @author Khue ng
 * @version 1.0
 */
public class Servlet_Element {

   /** The servlet name <servlet-name> **/
   private String m_Name;
   /** The display name <display-name> **/
   private String m_DisplayName;
   /** The desc <desc> **/
   private String m_desc;
   /** The servlet source <servlet-class> or <jsp-file> **/
   private String m_Source;
   /** The servlet Type , 1 = servlet , 2 = jsp **/
   private int m_Type;
   /** The servlet number , the order in which they are declared **/
   private int m_Number;


   /**
    * Constructor
    *
    * @param (String) path, the full path to the application.xml file
    */
    public Servlet_Element( String 	name,
                            String 	displayName,
                            String 	desc,
                            String 	source,
                            int    	type,
                            int 	servletNumber
                        ){

       m_Name            = name;
       m_DisplayName     = displayName;
       m_desc     = desc;
       m_Source          = source;
       m_Type            = type;
       m_Number			 = servletNumber;

    }


   /**
    * Return the servlet Name
    *
    * @return (String) the servlet Name
    */
   public String getName(){

      return m_Name;
   }


   /**
    * Set the servlet Name
    *
    * @param (String) the servlet Name
    */
   public void setName(String name){

      m_Name = name;
   }


   /**
    * Return the servlet display name
    *
    * @return (String) the servlet display name
    */
   public String getDisplayName(){

      return m_DisplayName;
   }


   /**
    * Set the servlet display name
    *
    * @param (String) the servlet display name
    */
   public void setDisplayName(String name){

      m_DisplayName = name;
   }


   /**
    * Return the servlet desc
    *
    * @return (String) the desc
    */
   public String getdesc(){

      return m_desc;
   }


   /**
    * Set the servlet desc
    *
    * @param (String) the desc
    */
   public void setdesc(String descr){

      m_desc = descr;
   }


   /**
    * Return the servlet source
    *
    * @return (String) the servlet source
    */
   public String getSource(){

      return m_Source;
   }


   /**
    * Set the servlet source ( a class or jsp file )
    *
    * @param (String) the servlet source
    */
   public void setSource(String servletsrc){

      m_Source = servletsrc;
   }


   /**
    * Return the servlet type
    *
    * @return (int) the servlet type
    */
   public int getType(){

      return m_Type;
   }


   /**
    * Return the servlet type label
    *
    * @return (String) the servlet type label
    */
   public String getTypeLabel(){

   		if ( m_Type==1 ){
      		return "Servlet";
      	} else {
      		return "Jsp";
      	}
   }


   /**
    * Set the servlet type ( 1 = servlet, 2 = jsp )
    *
    * @param (int) the servlet type
    */
   public void setType(int servletType){

      m_Type = servletType;
   }


   /**
    * Return the servlet number
    *
    * @return (int) the servlet number
    */
   public int getNumber(){

      return m_Number;
   }

   /**
    * Set the servlet number
    *
    * @param (int) the number
    */
   public void setNumber(int servletNumber){

      m_Number = servletNumber;
   }


} // end Servlet_Element
