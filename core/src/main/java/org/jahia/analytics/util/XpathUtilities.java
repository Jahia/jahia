/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.analytics.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.DOMAttrImpl;
import org.w3c.tidy.DOMCharacterDataImpl;
import org.w3c.tidy.DOMTextImpl;
import org.w3c.tidy.Tidy;

import java.util.ArrayList;

/**
 * The <code>XpathUtilities</code> class is used to easily extract elements and contents
 * from any pagesource. It is a collection of static methods which can be used to convert
 * and extract content.
 * 
 * @author Helge Staedtler
 */
public final class XpathUtilities {

	public XpathUtilities() {
		super();
	}

	public static String getContentForXpathInDOM( String xPath, Document dom ) throws Exception {
		Node nodeFound = getNodeForXpathInDOM( xPath, dom );
		return getContentFromNode( nodeFound );
	}
	
	/**
	 * @param node
	 * the node from which we want to get the content as string
	 * @return
	 * returns the content of this node as a string. because a node may have
	 * some attributes or some enclosed text, this method autoextracts the correct thing
	 */
	public static String getContentFromNode( Node node ) {
		String strToReturn = null;
		if( node == null ) return strToReturn;
		if( node != null ) {
			if( node instanceof DOMAttrImpl ) {
				strToReturn = ((DOMAttrImpl)node).getValue();
			}
			if( node instanceof DOMTextImpl ) {
				strToReturn = ((DOMTextImpl)node).getData();
			}
			if( node instanceof DOMCharacterDataImpl ) {
				strToReturn = ((DOMCharacterDataImpl)node).getData();
			}
		}
		return strToReturn;
	}
	
	/**
	 * Tries to find elements matching the defined xPath
	 * then returning the value of the first element found
	 * @param xPath
	 * the xpath string describing where in dom to locate the node
	 * @param dom
	 * W3C document which gets queried with the xpath to finde the exact node,
	 * in case more than one node matches, only the first node will be returned 
	 */
	public static Node getNodeForXpathInDOM( String xPath, Document dom ) throws Exception {
		ArrayList<Node> array = XpathUtilities.getNodesForXpathInDOM( xPath, dom );
		if( array == null || array.isEmpty() ) return null;
		return array.get(0); // return found node
	}
	
	/**
	 * Tries to extract matching nodes for defined xPath
	 * returns them in an array
	 * @param xPath
	 * the xpath string describing where in dom to locate the nodes
	 * @param dom
	 * W3C document which gets queried with the xpath to find the relevant nodes,
	 * all found/matching nodes are returned
	 */
	public static ArrayList<Node> getNodesForXpathInDOM( String xPath, Document dom ) throws Exception {
		ArrayList<Node> foundNodes = new ArrayList<Node>();
		
		if( xPath == null || xPath.length() <= 0 ) return foundNodes;
		// transform document in xpath'able stuff			    
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
	    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    XPath xpathInstance = XPathFactory.newInstance().newXPath();  
        NodeList nodes = null;
        try {
	        nodes = (NodeList) xpathInstance.evaluate( xPath, dom.getDocumentElement(), XPathConstants.NODESET );
	        Node currentNode = null;
	        for( int i = 0; i < nodes.getLength(); i++ ) {
	        	currentNode = nodes.item( i );
	        	foundNodes.add( currentNode );
	        }
        }
        catch( Exception ex ) {
        	throw ex;
        }
		return foundNodes;
	}
	
	/**
	 * tries to parse a DOM from some HTML-source
	 * @param sourceToTransform
	 * the source-code as a string which will be parsed
	 * we preprocess this with tidy to increase parsingsuccess
	 * @return returns a DOM if the parsing went alright
	 */
	public static Document getDomForSource( String sourceToTransform ) throws Exception {
		// now clean up and create document from the stuff
		  Tidy tidy = new Tidy();
		  tidy.setQuiet(true);
		  tidy.setShowWarnings(false);
		  tidy.setCharEncoding(Configuration.UTF8);
		  
		  Document dom = null;
		  try { // use tidy to get a DOM
			  dom = tidy.parseDOM( new ByteArrayInputStream( sourceToTransform.getBytes( "UTF-8" ) ), null);
		  }
		  catch( Exception ex ) {
			  throw ex;
		  }
		  return dom;
	}
	
	/**
	 * Just removes fancy tags before trying to parse the sourcecode
	 * getting rid of unnecessary and errorprone stuff may be better for tidy
	 * @param  sourceToTransform
	 * the pagesource/html we should parse
	 * @param listOfFancyTags
	 * a stringarray of tagnames which should be eliminated before we try to parse this stuff
	 * we use a regexpattern like regex = "(<tagName[^>]*>)|(</tagName[^>]*>)" to eliminate tags
	 * so start- and end-tags get eliminated but the content inbetween is left untouched
	 * @return a nice DOM if parsing went alright 
	 */
	public static Document getDomForSourceWithoutFancyTags( String sourceToTransform, String[] listOfFancyTags ) throws Exception {
		if( sourceToTransform == null || sourceToTransform.length() == 0 ) return null;
		  // now eliminate all the fancy display tags using regex patterns e.g. strong, em, and so on...
		  String preparedRegex = null;
		  // clean all those fancy tags
		  if( listOfFancyTags != null ) {
			  for( String currentTag : listOfFancyTags ) {
				  preparedRegex = "(<"+currentTag+"[^>]*>)|(</"+currentTag+"[^>]*>)";
				  sourceToTransform = Pattern.compile(preparedRegex, Pattern.CASE_INSENSITIVE).matcher(sourceToTransform).replaceAll("");
			  }
		  }
		  return XpathUtilities.getDomForSource(sourceToTransform);
	}
	
	/**
	 * returns the sourcecode for a DOM
	 * @param nodeToConvert
	 * some DOM-node
	 * @return
	 * a string representing the html-source for this node in utf-8
	 */
	public static String getSourceForNode( Node nodeToConvert ) throws Exception {
		if( nodeToConvert == null ) return null;
		String sourceGenerated = null;
		
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
	    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    
	    DOMSource miniDOM = new DOMSource( nodeToConvert );
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	serializer.transform( miniDOM, new StreamResult(new OutputStreamWriter( baos )));
    	sourceGenerated = baos.toString( );

    	return sourceGenerated;
	}
	
}
