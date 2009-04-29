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
//  Application_Xml
//
//  NK      29.01.2001
//
//

package org.jahia.data.webapps;


import java.util.ArrayList;
import java.util.List;

import org.jahia.data.xml.JahiaXmlDocument;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.xml.XMLParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Holds Informations about the Application deployment descriptors file
 * application.xml ( J2EE Standard )
 *
 * <application>
 *    <display-name>filemanager.ear</display-name>
 *    <desc>Application desc</desc>
 *    <module>
 *       <web>
 *          <web-uri>war-ic.war</web-uri>
 *          <context-root>filemanager</context-root>
 *       </web>
 *    </module>
 * </application>
 *
 *
 * @author Khue ng
 * @version 1.0
 */
public class Application_Xml extends JahiaXmlDocument {

   /** The J2EE Application Display Name  **/
   private String m_DisplayName;
   /** The J2EE Application desc **/
   private String m_desc;
   /** The list of Web Components *
    * @associates Web_Component*/
   private List<Web_Component> m_WebComponents = new ArrayList<Web_Component>();


   /**
    * Constructor
    *
    * @param (String) path, the full path to the application.xml file
    */
    public Application_Xml (String docPath) throws JahiaException
    {
      super(docPath);
    }


   	/**
     * Constructor using a gived parser
     * 
     * @param (String) path, the full path to a xml file
     * @param (Parser) parser, the parser to use
     */
	public Application_Xml (String docPath, org.xml.sax.helpers.ParserAdapter parser) 
	throws JahiaException {
		super(docPath,parser);
	}


   /**
    * Extracts data from the application.xml file. Build the JahiaWebAppsWarPackage object
    * to store extracted data
    */
   public void extractDocumentData() throws JahiaException {

      //JahiaConsole.println("Application_Xml::extractDocumentData","started");

       if (m_XMLDocument == null) {

          throw new JahiaException( "Application_Xml",
                                    "Parsed application.xml document is null",
                                       JahiaException.ERROR_SEVERITY,
                                       JahiaException.SERVICE_ERROR);
       }


       if (!m_XMLDocument.hasChildNodes()) {

          throw new JahiaException( "Application_Xml",
                                       "Main document node has no children",
                                        JahiaException.ERROR_SEVERITY,
                                        JahiaException.SERVICE_ERROR);
       }


      // get application node
      Element docElNode = (Element) m_XMLDocument.getDocumentElement();

       if (!docElNode.getNodeName().equalsIgnoreCase("application")) {

          throw new JahiaException(  "Invalid XML format",
                                        "application tag is not present as starting tag in file",
                                        JahiaException.ERROR_SEVERITY,
                                        JahiaException.SERVICE_ERROR);
       }


      // get module nodes
       List<Node> modNodes = XMLParser.getChildNodes(docElNode,"module");
       Node nodeItem = null;
       Node webNode = null;
       Node webURINode = null;
       Node contextNode = null;
       Node textNode = null;
      String webURI        = null;
      String contextRoot   = null;
      int size = modNodes.size();

      for ( int i=0 ; i<size; i++ ){

         nodeItem = (Node)modNodes.get(i);

         webNode = XMLParser.nextChildOfTag(nodeItem,"web");
         if (webNode != null ){
            webURINode = XMLParser.nextChildOfTag(webNode,"web-uri");
            if (webURINode != null ){
               textNode = webURINode.getFirstChild();
               if ( textNode != null ){
                  webURI = textNode.getNodeValue().trim();
               }
            }

            contextNode = XMLParser.nextChildOfTag(webNode,"context-root");
            if (contextNode != null ){
               textNode = contextNode.getFirstChild();
               if ( textNode != null){
                  contextRoot = textNode.getNodeValue().trim();
               } else {
                  contextRoot = "";
               }

            }

            if ( (webURI != null) && (webURI.length()>0) && contextRoot != null) {

               Web_Component webComp = new Web_Component( webURI, contextRoot );

               //JahiaConsole.println(">>"," Web Component Web URI        :" + webComp.getWebURI());
               //JahiaConsole.println(">>","               Context Root   :" + webComp.getContextRoot());

               m_WebComponents.add(webComp);
            }
         }
      }

      //JahiaConsole.println("Application_Xml::extractDocumentData","extraction done");
   }


   /**
    * Return the Display Name
    *
    * @return (String) the display name of the Application
    */
   public String getDisplayName(){

      return m_DisplayName;
   }


   /**
    * Set the DisplayName
    * @param (String) the display name of the webApp
    */
   protected void setDisplayName(String name){

      m_DisplayName = name;
   }

   /**
    * Return the Web App desc
    *
    * @return (String) the desc
    */
   public String getdesc(){

      return m_desc;
   }


   /**
    * Set the desc
    * @param (String) the desc
    */
   protected void setdesc(String descr){

      m_desc = descr;
   }


   /**
    * Return the list of Web Components
    *
    * @return (List) list of Web Components
    */
   public List<Web_Component> getWebComponents(){

      return m_WebComponents;

   }






} // end Application_Xml
