/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//  NK      02.02.2001
//

package org.jahia.services.webapps_deployer.tomcat;

import org.jahia.data.xml.JahiaXmlDocument;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.xml.XMLParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds Informations about the Tomcat tomcat-users.xml file
 * We need to modify this file to add the "manager" role to
 * use the Tomcat Management Application
 * <tomcat-users>
 * <user name="tomcat" password="tomcat" roles="tomcat,manager" />
 * <user name="role1"  password="tomcat" roles="role1"  />
 * <user name="both"   password="tomcat" roles="tomcat,role1" />
 * </tomcat-users>
 *
 * @author Khue ng
 * @version 1.0
 */
public class Tomcat_Users_Xml extends JahiaXmlDocument {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (Tomcat_Users_Xml.class);

    /** The list of Tomcat users Nodes * */
    private List m_UserNodes = new ArrayList();

    /**
     * Constructor
     *
     * @param docPath the full path to the tomcat-users.xml file
     */
    public Tomcat_Users_Xml (String docPath)
            throws JahiaException {
        super (docPath);
        extractDocumentData ();

    }

    /**
     * Extracts data from the Tomcat-users.xml file. Build the JahiaWebAppsWarPackage object
     * to store extracted data
     */
    public void extractDocumentData ()
            throws JahiaException {

        logger.debug ("started ");

        if (m_XMLDocument == null) {

            throw new JahiaException ("Tomcat_Users_Xml",
                    "Parsed Tomcat-users.xml document is null",
                    JahiaException.ERROR_SEVERITY,
                    JahiaException.SERVICE_ERROR);
        }

        if (!m_XMLDocument.hasChildNodes ()) {

            throw new JahiaException ("Tomcat_Users_Xml",
                    "Main document node has no children",
                    JahiaException.ERROR_SEVERITY,
                    JahiaException.SERVICE_ERROR);
        }

        // get <tomcat-users> node

        Element docElNode = (Element) m_XMLDocument.getDocumentElement ();

        if (!docElNode.getNodeName ().equalsIgnoreCase ("tomcat-users")) {

            throw new JahiaException ("Invalid XML format",
                    "tomcat-users tag is not present as starting tag in file",
                    JahiaException.ERROR_SEVERITY,
                    JahiaException.SERVICE_ERROR);
        }

        logger.debug ("Tomcat-users.xml file has tomcat-users element");

        // get users elements
        m_UserNodes = XMLParser.getChildNodes (docElNode, "user");

        logger.debug ("done");
    }

    /**
     * Return the list of Users Nodes
     *
     * @return (List) list of Users Nodes
     */
    public List getUserNodes () {

        return m_UserNodes;

    }

    /**
     * Append a new <user..> Element in the xml file
     * Add it in the user nodes list
     *
     * @param (String) name, tomcat user
     * @param (String) password
     * @param (String) roles , ex: "tomcat,manager"
     */
    public void addUser (
            String name,
            String password,
            String roles
            ) {

        Element docElNode = (Element) m_XMLDocument.getDocumentElement ();

        Element newNode = (Element) m_XMLDocument.createElement ("user");

        XMLParser.setAttribute (newNode, "name", name);
        XMLParser.setAttribute (newNode, "password", password);
        XMLParser.setAttribute (newNode, "roles", roles);

        if (m_UserNodes.size () > 0) {
            Node lastUserNode = (Node) m_UserNodes.get ((m_UserNodes.size () - 1));
            docElNode.insertBefore (newNode, lastUserNode);
        } else {
            docElNode.appendChild (newNode);
        }

        m_UserNodes.add (newNode);

    }

    /**
     * Change user password for a user with of a gived name and a having a gived role
     *
     * @param (String)  name tomcat user
     * @param (String)  password
     * @param (String)  role
     * @param (boolean) return false on error
     */
    public boolean updateUser (
            String name,
            String password,
            String role
            ) {

        int size = m_UserNodes.size ();
        for (int i = 0; i < size; i++) {
            Element user = (Element) m_UserNodes.get (i);

            if (name.equalsIgnoreCase (XMLParser.getAttributeValue (user, "name"))
                    &&
                    role.equalsIgnoreCase (XMLParser.getAttributeValue (user, "roles"))) {

                XMLParser.setAttribute (user, "password", password);
                return true;
            } else if (name.equalsIgnoreCase (XMLParser.getAttributeValue (user,
                    "username"))
                    &&
                    role.equalsIgnoreCase (XMLParser.getAttributeValue (user,
                            "roles"))) {
                XMLParser.setAttribute (user, "password", password);
                return true;

            }
        }

        return false;
    }

    /**
     * Check if a user exist
     *
     * @param (String) name, tomcat user
     * @param (String) password,
     */
    public boolean checkUser (
            String name,
            String password
            ) {

        int size = m_UserNodes.size ();
        for (int i = 0; i < size; i++) {
            Element user = (Element) m_UserNodes.get (i);

            if (name.equalsIgnoreCase (XMLParser.getAttributeValue (user, "name"))
                    &&
                    password.equalsIgnoreCase (XMLParser.getAttributeValue (user, "password"))) {
                return true;
            } else if (name.equalsIgnoreCase (XMLParser.getAttributeValue (user,
                    "username"))
                    &&
                    password.equalsIgnoreCase (XMLParser.getAttributeValue (user,
                            "password"))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return the password of a user with a gived name and a gived role
     *
     * @param (String) name, tomcat user
     * @param (String) role,
     */
    public String getUserPassword (String name, String role) {

        int size = m_UserNodes.size ();
        for (int i = 0; i < size; i++) {
            Element user = (Element) m_UserNodes.get (i);

            if (name.equalsIgnoreCase (XMLParser.getAttributeValue (user, "name")) &&
                (XMLParser.getAttributeValue (user, "roles") != null) &&
                (XMLParser.getAttributeValue (user, "roles").indexOf(role) != -1)) {
                return XMLParser.getAttributeValue (user, "password");

            } else if (name.equalsIgnoreCase (XMLParser.getAttributeValue (user,
                "username")) &&
                       (XMLParser.getAttributeValue (user, "roles") != null) &&
                       (XMLParser.getAttributeValue (user, "roles").indexOf(role) != -1)) {
                return XMLParser.getAttributeValue (user, "password");
            }
        }

        return null;
    }

}