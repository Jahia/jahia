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
//  MimeTypesFromWebAppXmlFile
//
//  NK      10.01.2002
//
//

package org.jahia.tools.files;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jahia.utils.JahiaConsole;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Load mime types declared in a Web.xml file
 *
 * <mime-mapping>
 *   <extension>abs</extension>
 *   <mime-type>audio/x-mpeg</mime-type>
 * </mime-mapping>
 * <mime-mapping>
 *   <extension>ai</extension>
 *   <mime-type>application/postscript</mime-type>
 * </mime-mapping>
 *
 * @author Khue ng
 * @version 1.0
 */
public class MimeTypesFromWebAppXmlFile {

    private static final String CLASS_NAME = MimeTypesFromWebAppXmlFile.class.getName();

    /** The xml Document **/
    protected Document m_XMLDocument;
    /** The Full Path to the xml file **/
    protected String m_DocPath;

    protected org.xml.sax.EntityResolver m_Resolver;


    private static final String WEB_APP_TAG = "web-app";
    private static final String MIME_MAPPING_TAG = "mime-mapping";
    private static final String EXTENSION_TAG = "extension";
    private static final String MIME_TYPE_TAG = "mime-type";

    private Properties m_MimeTypes = new Properties();

    //--------------------------------------------------------------------------
    /**
     * Handle xml document using default parser behavior
     *
     * @param (String) path, the full path to a xml file
     */
    public MimeTypesFromWebAppXmlFile (String docPath)
    throws Exception {
        m_DocPath = docPath;

        try {
                loadFile(m_DocPath);
        } catch ( Exception t ){
              throw new Exception(  CLASS_NAME
                                    + ", Exception while loading to the file"
                                    + m_DocPath + "\n"
                                    + t.getMessage() );
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Handle xml document using a gived parser
     *
     * @param (String) path, the full path to a xml file
     * @param (Parser) parser, the parser to use
     */
    public MimeTypesFromWebAppXmlFile (String docPath, org.xml.sax.EntityResolver entityResolver)
    throws Exception {

        m_Resolver = entityResolver;
        m_DocPath = docPath;

        try {
                loadFile(m_DocPath);
        } catch ( Exception t ){
              throw new Exception(  CLASS_NAME
                                    + ", Exception while loading to the file"
                                    + m_DocPath + "\n"
                                    + t.getMessage() );
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return a mime type looking at full file name
     *
     * @param String the file name
     * @return String the mime type or "" if not found
     */
    public String getMimeTypeFromFilename (String filename){

        if ( (m_MimeTypes == null)
                || (filename == null)
                || (filename.lastIndexOf(".") == -1) )
            return "";

        String ext = filename.substring( filename.lastIndexOf(".") + 1,
                                                 filename.length());

        return	getMimeTypeFromExt(ext);
    }

    //--------------------------------------------------------------------------
    /**
     * Return a mime type looking at the file extension without "."
     *
     * @param String the extension
     * @return String the mime type or "" if not found
     */
    public String getMimeTypeFromExt (String extension){

        if ( (m_MimeTypes == null)
                || (extension == null) )
            return "";

        String mimeType = "";

        mimeType = m_MimeTypes.getProperty(extension.toLowerCase());
        if ( mimeType == null )
            mimeType = "";

        return mimeType;
    }


    //--------------------------------------------------------------------------
    /**
     * Return the mimeTypes list as a Properties bean
     *
     * @return Properties mimeTypes
     */
    public Properties getMimeTypes (){

        return (Properties)m_MimeTypes.clone();
    }


    //--------------------------------------------------------------------------
    /**
     * Extract data from xml document.
     */
    public void extractDocumentData() throws Exception {

        if (m_XMLDocument == null) {

            throw new Exception ( CLASS_NAME + ", web.xml document is null" );
        }

        if (!m_XMLDocument.hasChildNodes()) {

            throw new Exception ( CLASS_NAME +
                                        ", Main document node has no children" );

        }

        // get web-app node
        Element        webAppNode;
        webAppNode = (Element) m_XMLDocument.getDocumentElement();


        if (!webAppNode.getNodeName().equalsIgnoreCase(WEB_APP_TAG)) {

            throw new Exception(  CLASS_NAME +
                        ", web-app tag is not present as starting tag in file" );
        }

        // build the mime mapping list
        List nodesList = getChildNodes(webAppNode,MIME_MAPPING_TAG);
        int size = nodesList.size();
        if ( size>0 ){

            Node nodeItem = null;
            String extension   = "";
            String mimeType   = "";

            Node currNode = null;

            for ( int i=0 ; i<size ; i++ ){
                nodeItem = (Node)nodesList.get(i);

                currNode = nextChildOfTag(nodeItem,EXTENSION_TAG);
                if (currNode != null ){
                    extension = currNode.getFirstChild().getNodeValue().trim();
                }

                currNode = nextChildOfTag(nodeItem,MIME_TYPE_TAG);
                if (currNode != null ){
                    mimeType = currNode.getFirstChild().getNodeValue().trim();
                }

                if ( extension != null && mimeType != null ){
                    m_MimeTypes.setProperty(extension.toLowerCase(),mimeType);
                    //System.out.println(CLASS_NAME+", added mime type :" + extension + "," + mimeType + "\n");
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    private void loadFile(String sourceFileName)
    throws ParserConfigurationException, Exception, IOException, org.xml.sax.SAXException {

        JahiaConsole.println(CLASS_NAME+".loadFile","sourceFileName=" + sourceFileName);

        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        //dfactory.setValidating(true); // create only parsers that are validating

        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        if ( m_Resolver != null ){
            docBuilder.setEntityResolver(m_Resolver);
        }
        FileInputStream sourceStream = new FileInputStream(sourceFileName);
        m_XMLDocument = docBuilder.parse(sourceStream);
        m_XMLDocument.normalize(); // clean up DOM tree a little

        extractDocumentData ();
    }

    //--------------------------------------------------------------------------
    /**
     * Get a List of child nodes equals with a gived tag
     *
     * @param (Node) startNode, the parent node
     * @param (String) tagName, the Children's tag name
     * @return (List) childs, a List of child node
     * @author NK
     */
    private List getChildNodes( 	Node parentNode,
                                    String tagName
                                    ) throws Exception {

        List childs = new ArrayList();

        NodeList nodeList = parentNode.getChildNodes();

        if ( nodeList != null ) {

            int size = nodeList.getLength();
            for ( int i=0; i<size ; i++ ){
                Node nodeItem = null;
                nodeItem = nodeList.item(i);
                /*
                JahiaConsole.println(">>", " getChildNodes, current child node = " + nodeItem.getNodeName() );
                */
                if ( nodeItem.getNodeName().equalsIgnoreCase(tagName) ){
                    childs.add(nodeItem);
                }
            }
        }

        return childs;
    }

    //--------------------------------------------------------------------------
    /**
     * nextChildOfTag
     * Go to the next Child Element Node that is equals
     * with the gived tag value
     *
     * @param (Node) startNode, the parent node
     * @param (String) tag, the tag name
     * @author NK
     */
    private Node nextChildOfTag( 	Node startNode,
                                    String tagName
                                 ) throws Exception {

        /*
        JahiaConsole.println(">>", " nextChildOfTag, tag " + tagName + " started ");
        */

        List childs = getChildNodes(startNode,tagName);
        int size = childs.size();
        for ( int i=0 ; i<size; i++ ){
            Node child = (Node)childs.get(i);
            if (child.getNodeName().equalsIgnoreCase(tagName)){
                /*
                JahiaConsole.println(">>", " nextChildOfTag, current child = " + child.getNodeName() );
                */
                return child;
            }
        }

        return null;
    }



} // end MimeTypesFromWebAppXmlFile
