/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

//
//
//  XMLParser
//
//  Loom    11.01.2001
//  NK      13.01.2001 Factorised as a utils class
//

package org.jahia.utils.xml;

import java.util.ArrayList;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Some tools for loading up data from an xml file
 *
 * @author Loom
 * @version 1.0
 */
public class XMLParser {

    private static final String ERROR_READING_FILE_MSG = "Error reading file";
    private static final String PARAMETER_TAG = "parameter";
    private static final String PARAMETER_TAG_NAME_ATTRIBUTE = "name";


    /**
     * Processes through all the parameter tags of a given node to find the value of
     * a certain named parameter
     *
     *
     * @return (String) the value of the parameter, null if not found , "" if value=empty
     */
    public static String getParameterValue( Node paramParent,
                                    String parameterName)
                                    throws JahiaException {

        if (!paramParent.hasChildNodes()) {
            throw new JahiaException("No parameters available on portlet XML tag",
                     "Parent has no children at all",
                     JahiaException.CRITICAL_SEVERITY,
                     JahiaException.CONFIG_ERROR);
        }

        Node curNode = paramParent.getFirstChild();
        while (curNode != null) {

            //JahiaConsole.println("XMLParser.getParameterValue", "Looking for param...["+curNode.getNodeName()+"]");
            // let's go through all the children nodes
            if (curNode.getNodeType() == Node.ELEMENT_NODE) {

                if (curNode.getNodeName().equalsIgnoreCase(PARAMETER_TAG)) {
                    // we have found a parameter tag, let's check further for match of param name
                    NamedNodeMap attr = curNode.getAttributes();
                    Node paramAttrNode = attr.getNamedItem(PARAMETER_TAG_NAME_ATTRIBUTE);
                    if (paramAttrNode != null) {

                        if (paramAttrNode.getNodeValue().equalsIgnoreCase(parameterName)) {
                            // we found the parameter
                            //JahiaConsole.println("XMLParser.getParameterValue", "Found parameter " + parameterName);
                            // we must now extract value of text node below this node.
                            Node textNode = curNode.getFirstChild();
                            if ( textNode == null ){ // check this otherwise nullpointer exception NK
                                return "";
                            } else {
                                if (textNode.getNodeType() == Node.TEXT_NODE) {
                                    return textNode.getNodeValue();
                                } else {
                                    throw new JahiaException(ERROR_READING_FILE_MSG,
                                    "Value of paramater is not in correct format, should only be text",
                                    JahiaException.CRITICAL_SEVERITY,
                                    JahiaException.CONFIG_ERROR);
                                }
                            }
                        }
                    } else {
                        throw new JahiaException(ERROR_READING_FILE_MSG,
                            "No attribute name found on parameter !",
                            JahiaException.CRITICAL_SEVERITY,
                            JahiaException.CONFIG_ERROR);
                    }
                }
            } else {
                // we just ignore other type of tags
            }
            curNode = curNode.getNextSibling();
       }

       return null; // better return null than throw an exception , NK ?

    }



    /**
     * nextChildOfTag
     * Go to the next Child Element Node that is equals
     * with the gived tag value
     *
     * @param (Node) startNode, the parent node
     * @param (String) tag, the tag name
     * @author NK
     */
    public static Node nextChildOfTag( Node startNode,
                              String tagName
                            ) throws JahiaException {

        /*
        JahiaConsole.println(">>", " nextChildOfTag, tag " + tagName + " started ");
        */

        List<Node> childs = getChildNodes(startNode,tagName);
        int size = childs.size();
        for ( int i=0 ; i<size; i++ ){
            Node child = childs.get(i);
            if (child.getNodeName().equalsIgnoreCase(tagName)){
                /*
                JahiaConsole.println(">>", " nextChildOfTag, current child = " + child.getNodeName() );
                */
                return child;
            }
        }

        return null;
    }


    /**
     * lastChildOfTag
     * Go to the last Child Element Node that is equals
     * with the gived tag value
     *
     * @param (Node) parentNode, the parent node
     * @param (String) tag, the tag name
     * @return (Node) the last child of this tag or null if not found
     * @author NK
     */
    public static Node lastChildOfTag( Node parentNode,
                              String tagName
                            ) throws JahiaException {

        /*
        JahiaConsole.println(">>", " nextChildOfTag, tag " + tagName + " started ");
        */

        List<Node> childs = getChildNodes(parentNode,tagName);
        int size = childs.size();
        for ( int i=0 ; i<size; i++ ){
            Node child = childs.get(i);
            if ( child.getNodeName().equalsIgnoreCase(tagName) && (i==size-1) ){
                /*
                JahiaConsole.println(">>", " nextChildOfTag, current child = " + child.getNodeName() );
                */
                return child;
            }
            return null;
        }

        return null;
    }


    /**
     * Get a List of child nodes equals with a gived tag
     *
     * @param (Node) startNode, the parent node
     * @param (String) tagName, the Children's tag name
     * @return (List) childs, a List of child node
     * @author NK
     */
    public static List<Node> getChildNodes( Node parentNode,
                                String tagName
                              ) throws JahiaException {

        List<Node> childs = new ArrayList<Node>();

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


    /**
     * Return the value of a Node's Attribute
     *
     * @param (Node) parentNode the parent node
     * @param (String) attributeName , the name of a gived attribute
     * @return (String) the value or null
     * @author NK
     */
    public static String getAttributeValue(Node parentNode, String attributeName){

        NamedNodeMap attribs = parentNode.getAttributes();
        //System.out.println(" node " + parentNode.getNodeName() + " has " + attribs.getLength() + " attributes") ;
        Node attribNode = attribs.getNamedItem(attributeName);
        if ( attribNode != null ){
            //System.out.println(" node " + parentNode.getNodeName() + ", " + attributeName + " =" + attribNode.getNodeValue() ) ;
            return attribNode.getNodeValue();
        }
        return null;
    } // end getAttributeValue()


   /**
    * Set the attibute of a node only if the value is not null or empty
    *
    * @param (ElementNode) a node
    * @param (String) attribName , the name of the attribute
    * @param (String) value, the value of the attribute
    */
   public static void setAttribute( Element nodeItem,
                             String attribName,
                             String value
                         ){

      if ( value != null && value.length()>0 ){
         nodeItem.setAttribute(attribName, value);
      }

   }



}
