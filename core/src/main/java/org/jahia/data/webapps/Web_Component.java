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
