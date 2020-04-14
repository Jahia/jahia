/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
//
//  NK      29.01.2001
//
//
package org.jahia.data.webapps;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds Informations about the Web Component deployment descriptors file web.xml
 *
 * @author Khue ng
 * @version 1.0
 */
public class Web_App_Xml {

    /**
     * The Servlet Type WebApp
     **/
    private static final int SERVLET_TYPE = 1;

    /**
     * The JSP Type WebApp
     **/
    private static final int JSP_TYPE = 2;

    /**
     * The Display Name
     **/
    private String displayName;

    /**
     * The desc
     **/
    private String desc;

    /**
     * The list of Servlet
     *
     * @associates Servlet_Element
     */
    private List<Servlet_Element> servlets = new ArrayList<>();

    /**
     * The hashMap of servlet mapping, keyed with the pattern used to map a servlet
     **/
    private Map<String, String> servletMappings = new HashMap<>();

    /**
     * The list of security-role
     **/
    private List<Security_Role> roles = new ArrayList<>();

    /**
     * The list of Welcome files
     **/
    private List<String> welcomeFiles = new ArrayList<>();

    private Document xmlDocument;

    /**
     * Constructor
     */
    private Web_App_Xml(Document xmlDocument) {
        super();
        this.xmlDocument = xmlDocument;
    }

    public static Web_App_Xml parse(InputStream stream) throws JahiaException {
        Web_App_Xml xml;
        try {
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

            dfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Document xmlDocument = dfactory.newDocumentBuilder().parse(stream);
            xmlDocument.normalize(); // clean up DOM tree a little
            xml = new Web_App_Xml(xmlDocument);
            xml.extractDocumentData();
        } catch (Exception t) {
            throw new JahiaException("JahiaXmlDocument",
                    "Exception while parsing web.xml stream",
                    JahiaException.ERROR_SEVERITY,
                    JahiaException.SERVICE_ERROR, t);
        }

        return xml;
    }

    // --------------------------------------------------------------------------

    /**
     * Extracts data from the web.xml file.
     */
    private void extractDocumentData() throws JahiaException {

        if (xmlDocument == null) {

            throw new JahiaException("Web_App_Xml",
                    "Parsed web.xml document is null",
                    JahiaException.ERROR_SEVERITY, JahiaException.CONFIG_ERROR);
        }

        if (!xmlDocument.hasChildNodes()) {

            throw new JahiaException("Web_App_Xml",
                    "Main document node has no children",
                    JahiaException.ERROR_SEVERITY, JahiaException.CONFIG_ERROR);
        }

        // get web-app node
        Element webAppNode;
        webAppNode = xmlDocument.getDocumentElement();

        if (!webAppNode.getNodeName().equalsIgnoreCase("web-app")) {

            throw new JahiaException("Invalid XML format",
                    "web-app tag is not present as starting tag in file",
                    JahiaException.ERROR_SEVERITY, JahiaException.CONFIG_ERROR);
        }

        // get the webapp display name
        Node displayNameNode = XMLParser.nextChildOfTag(webAppNode, "display-name");
        displayName = getValue(displayName, displayNameNode);

        // get the webapp description
        Node descriptionNode = XMLParser.nextChildOfTag(webAppNode, "description");
        desc = getValue(desc, descriptionNode);

        servlets = getServlets(webAppNode);
        servletMappings = getServletMappings(webAppNode);
        roles = getRoles(webAppNode);
        welcomeFiles = getWelcomeFiles(webAppNode);
    }

    /**
     * Return the Display Name
     *
     * @return (String) the display name of the Application
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the DisplayName
     *
     * @param name the display name of the webApp
     */
    protected void setDisplayName(String name) {
        displayName = name;
    }

    /**
     * Return the servlets list
     *
     * @return (List) the list of servlets
     */
    public List<Servlet_Element> getServlets() {
        return servlets;
    }

    /**
     * Return the map of servlet mapping
     *
     * @return (Map) the map of servlets mapping
     */
    public Map<String, String> getServletMappings() {
        return servletMappings;
    }

    /**
     * Return the welcome files list
     *
     * @return (List) the list of welcome files
     */
    public List<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    /**
     * return the List of roles
     *
     * @return (List) the roles
     */
    public List<Security_Role> getRoles() {
        return roles;
    }

    /**
     * Return the Web App desc
     *
     * @return (String) the desc
     */
    public String getdesc() {
        return desc;
    }

    /**
     * Set the desc
     *
     * @param descr the desc
     */
    protected void setdesc(String descr) {
        desc = descr;
    }

    // --------------------------------------------------------------------------

    /**
     * extract the list of roles
     */
    private List<Security_Role> getRoles(Node parentNode) throws JahiaException {

        List<Node> nodesList = XMLParser.getChildNodes(parentNode,
                "security-role");
        List<Security_Role> result = new ArrayList<>();

        int size = nodesList.size();
        if (size > 0) {

            Node nodeItem;
            String name;
            String descr;

            Node childNode;

            Security_Role role;

            for (int i = 0; i < size; i++) {

                name = "";
                descr = "";
                nodeItem = nodesList.get(i);

                childNode = XMLParser.nextChildOfTag(nodeItem, "role-name");
                name = getValue(name, childNode);

                childNode = XMLParser.nextChildOfTag(nodeItem, "desc");
                descr = getValue(descr, childNode);

                if (name.length() > 0) {
                    role = new Security_Role(name, descr);
                    result.add(role);
                }
            }
        }
        return result;
    }

    // --------------------------------------------------------------------------

    /**
     * Extract the list of servlets
     */
    private List<Servlet_Element> getServlets(Node parentNode)
            throws JahiaException {

        List<Servlet_Element> result = new ArrayList<>();

        // build the servlets list
        List<Node> nodesList = XMLParser.getChildNodes(parentNode, "servlet");
        int size = nodesList.size();
        if (size > 0) {

            Node nodeItem;
            String servletDisplayName;
            String servletName = "";
            String descr = "";
            String servletsrc ;
            int servletType;

            Node childNode;

            for (int i = 0; i < size; i++) {
                nodeItem = nodesList.get(i);

                childNode = XMLParser.nextChildOfTag(nodeItem, "servlet-name");
                servletName = getValue(servletName, childNode);

                childNode = XMLParser.nextChildOfTag(nodeItem, "display-name");
                if (childNode != null) {
                    servletDisplayName = childNode.getFirstChild().getNodeValue()
                            .trim();
                } else {
                    servletDisplayName = servletName;
                }

                childNode = XMLParser.nextChildOfTag(nodeItem, "desc");
                descr = getValue(descr, childNode);

                if (XMLParser.nextChildOfTag(nodeItem, "servlet-class") != null) {
                    servletsrc = XMLParser.nextChildOfTag(nodeItem,
                            "servlet-class").getFirstChild().getNodeValue()
                            .trim();
                    servletType = SERVLET_TYPE;
                } else {
                    servletsrc = XMLParser.nextChildOfTag(nodeItem, "jsp-file")
                            .getFirstChild().getNodeValue().trim();
                    servletType = JSP_TYPE;
                }

                if ((servletDisplayName != null) && (servletDisplayName.length() > 0)
                        && (servletName != null) && (servletName.length() > 0)
                        && (servletsrc != null) && (servletsrc.length() > 0)) {

                    Servlet_Element servlet = new Servlet_Element(servletName,
                            servletDisplayName, descr, servletsrc, servletType, i + 1);

                    result.add(servlet);
                }
            }
        }
        return result;
    }

    private String getValue(String servletName, Node childNode) {
        if (childNode != null) {
            servletName = childNode.getFirstChild().getNodeValue().trim();
        }
        return servletName;
    }

    // --------------------------------------------------------------------------

    /**
     * Extract the list of welcome files
     */
    private List<String> getWelcomeFiles(Node parentNode) throws JahiaException {

        List<String> results = new ArrayList<>();

        // get the welcome-file-list element
        Node welcomeFileListNode = XMLParser.nextChildOfTag(parentNode, "welcome-file-list");
        if (welcomeFileListNode == null) {
            return results;
        }

        List<Node> nodesList = XMLParser.getChildNodes(welcomeFileListNode, "welcome-file");

        int size = nodesList.size();
        if (size > 0) {

            Node nodeItem = null;
            String filename = "";

            for (int i = 0; i < size; i++) {

                nodeItem = nodesList.get(i);
                filename = nodeItem.getFirstChild().getNodeValue().trim();

                if (filename.length() > 0) {
                    results.add(filename);
                }
            }
        }
        return results;
    }

    // --------------------------------------------------------------------------

    /**
     * Extract a map of servlet mapping.
     */
    private Map<String, String> getServletMappings(Node parentNode)
            throws JahiaException {

        Map<String, String> result = new HashMap<>();

        List<Node> nodesList = XMLParser.getChildNodes(parentNode,
                "servlet-mapping");

        int size = nodesList.size();
        if (size > 0) {

            Node nodeItem;
            String servletName;
            String urlPattern;

            Node childNode;

            for (int i = 0; i < size; i++) {

                servletName = "";
                urlPattern = "";

                nodeItem = nodesList.get(i);

                childNode = XMLParser.nextChildOfTag(nodeItem, "servlet-name");
                servletName = getValue(servletName, childNode);

                childNode = XMLParser.nextChildOfTag(nodeItem, "url-pattern");
                urlPattern = getValue(urlPattern, childNode);

                if (servletName != null && (servletName.length() > 0) && urlPattern != null && (urlPattern.length() > 0)) {
                    result.put(urlPattern, servletName);
                }
            }
        }

        return result;
    }

} // end Web_App_Xml
