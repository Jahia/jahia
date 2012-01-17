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
//  JahiaWebAppDef
//
//  NK      16.01.2001
//
//

package org.jahia.data.webapps;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds Informations about a WebApp Definition
 *
 * A web app contains a set of servlet or portlet elements
 * Example for a servlet :
 * <servlet>
 *   <servlet-name>FilemanagerServlet</servlet-name>
 *   <display-name>Filemanager Portlet</display-name>
 *   <desc>no desc</desc>
 *   <servlet-class>org.jahia.portlets.filemanager.FilemanagerServlet</servlet-class>
 *   <init-param>
 *     <param-name>properties</param-name>
 *     <param-value>WEB-INF/conf/filemanager.properties</param-value>
 *   </init-param>
 * </servlet>
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaWebAppDef {

    /** The Display name of the web App **/
    private String m_Name = "";

    /** The desc **/
    private String m_desc = "";

    /** The Context Root Folder **/
    private String m_ContextRoot;

    /** The set of servlets *
     * @associates Servlet_Element*/
    private List m_Servlets = new ArrayList();

    /** The set of roles *
     * @associates Security_Role*/
    private List m_Roles = new ArrayList();
    private String type;

    /**
     * Constructor
     *
     */
    public JahiaWebAppDef (
        String name,
        String contextRoot, String type) {
        m_Name = name;
        m_ContextRoot = contextRoot;
        this.type = type;
    }

    /**
     * Return the webApp display name
     *
     * @return (String) the name of the webApp
     */
    public String getName () {

        return m_Name;
    }

    /**
     * Set the display name
     * @param (String) the name of the webApp
     */
    public void setName (String name) {

        m_Name = name;
    }

    /**
     * Return the Context Root
     *
     * @return (String) the context root
     */
    public String getContextRoot () {

        return m_ContextRoot;
    }

    /**
     * Set the Context Root
     * @param (String) the Context Root
     */
    public void setContextRoot (String contextRoot) {

        m_ContextRoot = contextRoot;
    }

    /**
     * Return the Web App desc
     *
     * @return (String) the desc
     */
    public String getdesc () {

        return m_desc;
    }

    /**
     * Set the desc
     * @param (String) the desc
     */
    public void setdesc (String descr) {

        m_desc = descr;
    }

    /**
     * add a Servlet Element
     *
     * @param (Servlet_Element) a servlet element
     */
    public void addServlet (Servlet_Element servlet) {
        m_Servlets.add(servlet);
    }

    /**
     * append a List of Servlet Element at the end of the servlets list
     *
     * @param (List) a List of servlet element
     */
    public void addServlets (List servlets) {
        m_Servlets.addAll(servlets);
    }

    /**
     * get a List of the Servlets
     *
     * @return (List) return an Iterator of servlets
     */
    public List getServlets () {
        return m_Servlets;
    }

    /**
     * add a Security_Role
     *
     * @param (Security_Role) a role element
     */
    public void addRole (Security_Role role) {
        m_Roles.add(role);
    }

    /**
     * append a List of Roles  at the end of the roles list
     *
     * @param (List) a List of role element
     */
    public void addRoles (List roles) {
        m_Roles.addAll(roles);
    }

    /**
     * get a List of the roles
     *
     * @return (Iterator) return the Iterator of Roles
     */
    public Iterator getRoles () {
        return m_Roles.iterator();
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

} // end JahiaWebAppDef
