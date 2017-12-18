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
//                       __/\ ______|    |__/\.     _______
//            __   .____|    |       \   |    +----+       \
//    _______|  /--|    |    |    -   \  _    |    :    -   \_________
//   \\______: :---|    :    :           |    :    |         \________>
//           |__\---\_____________:______:    :____|____:_____\
//                                      /_____|
//
//                 . . . i n   j a h i a   w e   t r u s t . . .
//
//
//
//
//  NK      18.06.2002
//
//

package org.jahia.data.applications;

import org.jahia.utils.InsertionSortedMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Holds information for a given web application context.
 * Some of them are: servlets, roles, servlet mapping, welcome files.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.com">khue@jahia.com</a>
 */
public class WebAppContext implements Serializable
{
    private static final long serialVersionUID = 5206818081114734385L;

    /** the application display name **/
    private String displayName = "";

    /** the application context **/
    private String context = "";

    /** the application description **/
    private String descr = "";

    /** The Map of servlet bean , keyed by the servlet name **/
    private InsertionSortedMap<String, ServletBean> servlets = new InsertionSortedMap<String, ServletBean>();
    private static final Object servletsLock = new Object();

    /**
     * The hashMap of servlet mapping, keyed with the pattern used to map a servlet.
     * The value is the servlet name.
     **/
    private Map<String, String> servletMappings = new HashMap<String, String>();
    private static final Object mappingsLock = new Object();

    /** List of security roles **/
    private List<String> roles = new ArrayList<String>();
    private static final Object rolesLock = new Object();

    /** The list of Welcome files **/
    private List<String> welcomeFiles = new ArrayList<String>();
    private static final Object welcomesLock = new Object();

    // Entry points into servlet-based portlet web application.
    private List<EntryPointDefinition> entryPoints = new ArrayList<EntryPointDefinition>();



    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     */
    public WebAppContext(String context){
        this.context = context;
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param context , the application context
     * @param displayName , the application display name
     * @param descr, the application description
     * @param servlets, a List ServletBean
     * @param servletMappings, a map of servletMappings keyed with the url pappern and value = the servlet name
     * @param welcomeFiles, a List of welcome files (String)
     */
    public WebAppContext( 	String context,
                                String displayName,
                                String descr,
                                List<ServletBean> servlets,
                                Map<String, String> servletMappings,
                                List<String> roles,
                                List<String> welcomeFiles )
    {
        this.context = context;

        if ( displayName != null ){
            this.displayName = displayName;
        }
        if ( descr != null ){
            this.descr = descr;
        }
        if (servlets != null) {
            addServlets(servlets);
        }
        if (servletMappings != null) {
            setServletMappings(servletMappings);
        }
        setRoles(roles);
        setWelcomeFiles(welcomeFiles);
    }

    //--------------------------------------------------------------------------
    /**
     * Add a List of ServletBean.
     *
     * @param servlets the ServletBeans to add
     */
    public void addServlets(List<ServletBean> servlets) {
        synchronized (servletsLock) {
            if ( servlets!=null ){
                for (ServletBean servlet : servlets) {
                    if (servlet != null && servlet.getServletName() != null) {
                        this.servlets.put(servlet.getServletName(), servlet);
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new servlet bean.
     *
     * @param servlet the ServletBean to add
     */
    public void addServlet(ServletBean servlet) {
        synchronized (servletsLock) {
            if ( servlet!=null && servlet.getServletName() != null ){
                servlets.put(servlet.getServletName(), servlet);
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Get a servlet looking at it name.
     *
     * @param name , the servlet name
     */
    public ServletBean getServlet(String name) {
        synchronized (servletsLock) {
            if ( name!=null){
                return servlets.get(name);
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the map of servlet mapping.
     *
     * @param servletMappings a map of servlet mappings keyed with the url pattern and the servlet name as value
     */
    public void setServletMappings(Map<String, String> servletMappings) {
        synchronized (mappingsLock) {
            if ( servletMappings != null ){
                this.servletMappings = servletMappings;
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new servlet mapping and replace old mapping with same pattern.
     *
     * @param pattern the URL pattern for which we want to add a mapping
     * @param name the name of the servlet mapped to the specified URL pattern
     */
    public void addServletMapping(String pattern, String name) {
        synchronized (mappingsLock) {
            servletMappings.put(pattern, name);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return the servlet name mapped by the given pattern
     * otherwise return null.
     *
     * @param pattern the url-pattern
     */
    public String findServletMapping(String pattern) {
        synchronized (mappingsLock) {
            return servletMappings.get(pattern);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return the List of servlets.
     *
     * @return servlets
     */
    public Map<String, ServletBean> getServlets() {
        synchronized (servletsLock) {
            return servlets;
        }
    }


    //--------------------------------------------------------------------------
    /**
     * Set roles.
     *
     * @param roles a List of security roles
     */
    public void setRoles(List<String> roles) {
        synchronized (rolesLock) {
            if ( roles != null ){
                this.roles = roles;
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Add a new security role.
     *
     * @param role New security role
     */
    public void addRole(String role) {
        synchronized (rolesLock) {
            roles.add(role);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return true if a given role is already in the list.
     *
     * @param role the security role to look for
     */
    public boolean findRole(String role) {
        if ( role == null ){
            return false;
        }
        synchronized (rolesLock) {
            return roles.contains(role);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return the security roles defined for this application.
     */
    public List<String> getRoles() {
        synchronized (rolesLock) {
            return roles;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Set the welcome files.
     *
     * @param welcomeFiles a List of welcome file names
     */
    public void setWelcomeFiles(List<String> welcomeFiles) {
        synchronized (welcomesLock) {
            if ( welcomeFiles != null ){
                this.welcomeFiles = welcomeFiles;
            }
        }
    }


    //--------------------------------------------------------------------------
    /**
     * Add a new welcome file.
     *
     * @param filename New welcome file name
     */
    public void addWelcomeFile(String filename) {
        synchronized (welcomesLock) {
            welcomeFiles.add(filename);
        }
    }

    public List<EntryPointDefinition> getEntryPointDefinitions() {
        return entryPoints;
    }

    public void setEntryPointDefinitions(List<EntryPointDefinition> entryPointDefinitions) {
        this.entryPoints = entryPointDefinitions;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the list of welcome file.
     *
     * @return welcomeFiles
     */
    public List<String> getWelcomeFiles() {
        synchronized (welcomesLock) {
            return welcomeFiles;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Return the context
     *
     */
    public String getContext(){
        return context;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the context
     *
     * @param val the new context
     */
    public void setContext(String val){
        context = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the display name
     *
     */
    public String getDisplayName(){
        return displayName;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the display name
     *
     * @param val the new display name
     */
    public void setDisplayName(String val){
        displayName = val;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the descr
     *
     */
    public String getDescr(){
        return descr;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the descr
     *
     * @param val the new description
     */
    public void setDescr(String val){
        descr = val;
    }


}
