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
//

package org.jahia.data.viewhelper.principal;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletRequest;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.GuestGroup;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerDBProvider;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.UsersGroup;
import org.jahia.utils.JahiaString;
import org.jahia.utils.JahiaTools;

/**
 * <p>Title: Principal output formating view helper</p>
 * <p>Description:
 * The role of this class is to prepare and format user and group datas for
 * display according to the JSP files needs in administration and engines.</p>
 *
 * The output can be formated to the following string sequence :<br>
 *      {"Principal", "Permissions", "Provider, 6", "Name, 10", "Properties, 20"]<br><br>
 * The digit after identifiers are the number character for the output string.
 * Where :
 *      <li>Principal, indicate if the pricipal is a user(u) or a group(g)<br>
 *      <li>Permissions, is the permissions sequence : Admin, write, rights accesses<br>
 *      <li>Pprovider, is the source where the user and the group are coming from<br>
 *      <li>Name, is the username or the groupname<br>
 *      <li>Properties, is the user firstname following by the user lastname or
 *          the user e-mail. Is the group member name for the group.<br><br>
 *
 * Example :<br>
 *      u jahia  -wr  gdupont      G?rard Dupont<br>
 *
 * <p>Copyright: MAP (Jahia Solutions S?rl 2003)</p>
 * <p>Company: Jahia Solutions S?rl</p>
 * @author MAP
 * @version 1.0
 */
public class PrincipalViewHelper implements Serializable {

    public static final String PRINCIPAL = "Principal";
    public static final String PERMISSIONS = "Permissions";
    public static final String PROVIDER = "Provider";
    public static final String SITEID = "SiteID";
    public static final String SITEKEY = "SiteKey";
    public static final String SITETITLE = "SiteTitle";
    public static final String NAME = "Name";
    public static final String PROPERTIES = "Properties";
    public static final String INHERITANCE = "Inheritance";

    private Map perms;
    private Set inheritance;

    private static Set selectBoxFieldsHeading = new HashSet();
    private List selectBoxFieldsSize = new ArrayList();
    private List selectBoxFieldsMethod = new ArrayList();
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(PrincipalViewHelper.class);
    
    static {
        selectBoxFieldsHeading.add(PRINCIPAL);
        selectBoxFieldsHeading.add(PERMISSIONS);
        selectBoxFieldsHeading.add(PROVIDER);
        selectBoxFieldsHeading.add(SITEID);
        selectBoxFieldsHeading.add(SITEKEY);
        selectBoxFieldsHeading.add(SITETITLE);
        selectBoxFieldsHeading.add(NAME);
        selectBoxFieldsHeading.add(PROPERTIES);
        selectBoxFieldsHeading.add(INHERITANCE);
    }
    
    /**
     * Create the view helper with a given string format given by the following
     * syntax :
     * textFormat ::= (principal)? (permissions)? (provider)? (name)? (properties)?
     * principal ::= "Principal"
     * permissions ::= "Permissions"
     * provider ::= "Provider," size
     * name ::= "Name," size
     * properties ::= "Properties," size
     * size ::= number{2..n}
     *
     * @param textFormat The string format given by the above syntax.
     */
    public PrincipalViewHelper(String[] textFormat) {
        for (int i = 0; i < textFormat.length; i++) {
            final StringTokenizer st = new StringTokenizer(textFormat[i], ",");
            final String fieldToDisplay = (String)st.nextElement();
            if (selectBoxFieldsHeading.contains(fieldToDisplay)) {
                if (st.hasMoreElements()) {
                    selectBoxFieldsSize.add(Integer.valueOf(((String)st.nextElement()).trim()));
                } else {
                    selectBoxFieldsSize.add(new Integer(-1));
                }
                try {
                    selectBoxFieldsMethod.add(PrincipalViewHelper.class.getMethod(
                        "get" + fieldToDisplay, new Class[] {Principal.class, Integer.class}));
                } catch (java.lang.NoSuchMethodException nsme) {
                    logger.fatal("Internal class error ! Please check Jahia code", nsme);
                }
            } 
        }
    }

    /**
     * Prepare the princiapl string output for display according to the "textFormat".
     * N.B. Usually used in Jahia for the HTML "select" tag.
     *
     * @param p The principal (user or group) to format
     * @return The principal formated
     */
    public String getPrincipalTextOption(Principal p) {
        final StringBuffer authUserText = new StringBuffer();
        for (int i = 0; i < selectBoxFieldsMethod.size(); i++) {
            final Method m = (Method)selectBoxFieldsMethod.get(i);
            final Integer size = (Integer)selectBoxFieldsSize.get(i);
            final Object[] args = { p, size };
            try {
                authUserText.append((String)m.invoke(this, args));
            } catch (java.lang.reflect.InvocationTargetException ite) {
                logger.fatal("Internal class error !", ite);
            } catch (java.lang.IllegalAccessException iae) {
                logger.fatal("Internal class error !", iae);
            }
            if (i + 1 < selectBoxFieldsHeading.size()) {
                authUserText.append(" ");
            }
        }
        return JahiaTools.replacePattern(authUserText.toString(), " ", "&nbsp;");
    }

    /**
     * Prepare the "value" string output.
     * N.B. Usually used in Jahia for the HTML "select" tag.
     *
     * @param p The principal (user or group) to format
     * @return The user/group key depending from principal type.
     */
    public String getPrincipalValueOption(Principal p) {
        final StringBuffer buff = new StringBuffer();
        if (p == null) {
            return "null";
        }
        if (p instanceof JahiaUser) {
            return buff.append("u").append(((JahiaUser)p).getUserKey()).toString();
        } else {
            return buff.append("g").append(((JahiaGroup)p).getGroupKey()).toString();
        }
    }

    /**
     * Construct a displayable principal name string
     * @param p The user object
     * @param size The principal string size that should be displayed.
     * @return The displayable principal string.
     */
    public static String getName(Principal p, Integer size) {
        if (p instanceof JahiaUser) {
            return JahiaString.adjustStringSize(((JahiaUser)p).getUsername(), size.intValue());
        } else {
            return JahiaString.adjustStringSize(((JahiaGroup)p).getGroupname(), size.intValue());
        }
    }

    /**
     * Construct a displayable provider name string
     * @param p The principal object
     * @param size The provider string size that should be displayed.
     * @return The displayable provider string.
     */
    public static String getProvider(Principal p, Integer size) {
        if (p instanceof JahiaUser) {
            return JahiaString.adjustStringSize(((JahiaUser)p).getProviderName(),
                                                size.intValue());
        } else {
            return JahiaString.adjustStringSize(((JahiaGroup)p).getProviderName(),
                                                size.intValue());
        }
    }

    /**
     * Construct a displayable site ID string
     * @param p The principal object
     * @param size The provider string size that should be displayed.
     * @return The displayable id string.
     */
    public static String getSiteID(Principal p, Integer size) {
        if (p instanceof JahiaUser) {
            return StringUtils.repeat(" ", size);
        } else {
            return JahiaString.adjustStringSize(Integer.toString(((JahiaGroup)p).getSiteID()),
                                                size.intValue());
        }
    }

    /**
     * Construct a displayable site key string
     * @param p The principal object
     * @param size The provider string size that should be displayed.
     * @return The displayable site key string
     */
    public static String getSiteKey(Principal p, Integer size) {
        int siteID = -1;
        if (p instanceof JahiaUser) {
            siteID = 0;
        } else {
            siteID = ((JahiaGroup)p).getSiteID();
        }
        if (siteID == 0) {
            return JahiaString.adjustStringSize("server", size.intValue());
        }
        if (siteID > 0) {
            try {
                JahiaSite jahiaSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteID);
                return JahiaString.adjustStringSize(jahiaSite.getSiteKey(), size.intValue());
            } catch (JahiaException je) {
                logger.error ("Error while retrieving site id=" + siteID, je);
            }
        }
        return JahiaString.adjustStringSize("unknown", size.intValue());
    }

    /**
     * Construct a displayable site title string
     * @param p The principal object
     * @param size The provider string size that should be displayed.
     * @return The displayable site title string
     */
    public static String getSiteTitle(Principal p, Integer size) {
        int siteID = -1;
        if (p instanceof JahiaUser) {
            siteID = 0;
        } else {
            siteID = ((JahiaGroup)p).getSiteID();
        }
        if (siteID == 0) {
            return JahiaString.adjustStringSize("server", size.intValue());
        }
        if (siteID > 0) {
            try {
                JahiaSite jahiaSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteID);
                return JahiaString.adjustStringSize(jahiaSite.getTitle(), size.intValue());
            } catch (JahiaException je) {
                logger.error ("Error while retrieving site id=" + siteID, je);
            }
        }
        return JahiaString.adjustStringSize("unknown", size.intValue());
    }

    /**
     * Get the kind of principal given by one char u = user, g = group
     * @param p The principal object
     * @param size For method call compatibility
     * @return The principal type
     */
    public static String getPrincipal(Principal p, Integer size) {
        if (p instanceof JahiaUser) {
            return "u";
        } else {
            return "g";
        }
    }

    /**
     * Get a principal displayable properties. A user displays its
     * first name and a last name or e-mail to display.
     * A group should displays its users or groups.
     *
     * @param p The principal object
     * @param size The size the properties should be displayed
     * @return The displayable properties.
     */
    public static String getProperties(Principal p, Integer size) {
        final StringBuffer properties = new StringBuffer();
        if (p instanceof JahiaUser) {
            final JahiaUser user = (JahiaUser)p;
            // Find a displayable user property
            if (user.getUsername().equals(JahiaUserManagerDBProvider.GUEST_USERNAME)) {
                properties.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.users.guest.label", Jahia.getThreadParamBean().getLocale()));
                return JahiaString.adjustStringSize(properties.toString(), size);
            } else {
                final String firstname = user.getProperty("firstname");
                final String lastname = user.getProperty("lastname");
                if (firstname != null) {
                    properties.append(firstname);
                    if (firstname.length() < size) {
                        properties.append(" ");
                    }
                }
                if (lastname != null)
                    properties.append(lastname);
                if ("".equals(properties.toString())) {
                    String email = user.getProperty("email");
                    if (email != null)
                        properties.append(email);
                }
                return JahiaString.adjustStringSize(properties.toString(), size);
            }
        } else if (p instanceof UsersGroup) {
            properties.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.groups.users.label", Jahia.getThreadParamBean().getLocale()));
            return JahiaString.adjustStringSize(properties.toString(), size);
        } else if (p instanceof GuestGroup) {
            properties.append(JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.groups.guest.label", Jahia.getThreadParamBean().getLocale()));
            return JahiaString.adjustStringSize(properties.toString(), size);
        } else {
            final JahiaGroup group = (JahiaGroup)p;
            // Find some group members for properties
            final Iterator grpMembers = group.isPreloadedGroups() ? new EnumerationIterator(group.members()) : null;
            final StringBuffer members = new StringBuffer().append("(");
            if (grpMembers != null) {
                while (grpMembers.hasNext()) {
                    final Object obj = grpMembers.next();
                    if (obj instanceof JahiaUser) {
                        final JahiaUser tmpUser = (JahiaUser)obj;
                        members.append(tmpUser.getUsername());
                    } else {
                        final JahiaGroup tmpGroup = (JahiaGroup)obj;
                        members.append(tmpGroup.getGroupname());
                    }
                    if (members.length() > size.intValue())
                        break;
                    if (grpMembers.hasNext())
                        members.append(", ");
                }
            } else {
                members.append("...");
            }
            members.append(")");
            return JahiaString.adjustStringSize(members.toString(), size.intValue());
        }
    }

    /**
     * Translate the ACL entry permissions setting to a string.
     * @param p   the user/group which have permissions
     * @return    a string permissions.
     */
    public String getPermissions(Principal p, Integer size) {
        if (size == -1) {
            size = 3;
        }
        final Integer permissions = ((Integer[]) perms.get(p))[0];

        final StringBuffer permStr = new StringBuffer();

        final boolean inherited = (permissions.intValue() >> 3 & 1) != 0;
        final boolean linked = (permissions.intValue() >> 4 & 1) != 0;
        char[] perms = {'R','W', 'A'};
        for (int i = 0; i<size; i++) {
            permStr.append((permissions.intValue() >> i & 1) != 0 ? perms[i] : "-");
        }
        for (int i = size; i<3; i++) {
            permStr.append(" ");            
        }
        if (linked) {
            permStr.append("+");
        } else if (inherited) {
            permStr.append("*");
        } else {
            permStr.append("&nbsp;");
        }

        return permStr.toString();
    }

    /**
     *
     */
    public void setPermissions(Map perms) {
        this.perms = perms;
    }

    public String getInheritance(Principal p, Integer size) {
        if (inheritance.contains(p)) {
            return "*&nbsp;";
        }
        else return "&nbsp;&nbsp;";
    }

    public void setInheritance(Set inheritance) {
        this.inheritance = inheritance;
    }
    /**
     * Get the user search result from the parameter form given by the request.
     * If the form is not in the request then all the Jahia users will be search.
     *
     * @param processingContext the context that should contain the HTML formular with the
     * following fields :
     * - searchString
     * - searchIn
     * - properties
     * - storedOn
     * - providers
     * @param siteID The site ID containing the principal to search
     * @return a Properties object that contain the search criterium
     */
    public static Set getSearchResult(ProcessingContext processingContext, int siteID) {

        String searchString = processingContext.getParameter("searchString");
        String searchIn = processingContext.getParameter("searchIn");
        String[] searchInProps = processingContext.getParameterValues("properties");
        String storedOn = processingContext.getParameter("storedOn");
        String[] providers = processingContext.getParameterValues("providers");

        return getSearchResult(searchIn, siteID, searchString, searchInProps, storedOn, providers);
    }

    /**
     * Get the user search result from the parameter form given by the request.
     * If the form is not in the request then all the Jahia users will be search.
     *
     * @param request the request that should contain the HTML formular with the
     * following fields :
     * - searchString
     * - searchIn
     * - properties
     * - storedOn
     * - providers
     * @param siteID The site ID containing the principal to search
     * @return a Properties object that contain the search criterium
     */
    public static Set getSearchResult(ServletRequest request, int siteID) {

        String searchString = request.getParameter("searchString");
        final String searchIn = request.getParameter("searchIn");
        final String[] searchInProps = request.getParameterValues("properties");
        final String storedOn = request.getParameter("storedOn");
        final String[] providers = request.getParameterValues("providers");

        return getSearchResult(searchIn, siteID, searchString, searchInProps, storedOn, providers);
    }

    public static Set getSearchResult(String searchIn, int siteID, String searchString, String[] searchInProps, String storedOn, String[] providers) {
        JahiaUserManagerService jahiaUserManagerService =
            ServicesRegistry.getInstance().getJahiaUserManagerService();
        final Properties searchParameters = new Properties();
        final Set searchResults = new HashSet();
        if (searchIn == null) { // Necessary condition to say there is no formular.
            logger.debug("No formular transmited. Finding all Jahia DB users.");
            searchParameters.setProperty("*", "*");
            searchResults.addAll(jahiaUserManagerService.
                searchUsers(siteID, searchParameters));
        } else {
            //if (searchString == null || "".equals(searchString)) {
            if ("".equals(searchString)) {
                searchString = "*";
            }
            if ("allProps".equals(searchIn) || searchInProps == null) {
                searchParameters.setProperty("*", searchString);
            } else {
                for (int i = 0; i < searchInProps.length; i++) {
                    searchParameters.setProperty(searchInProps[i], searchString);
                }
            }
            if ("everywhere".equals(storedOn) || providers == null) {
                searchResults.addAll(jahiaUserManagerService.
                    searchUsers(siteID, searchParameters));
            } else {
                for (int i = 0; i < providers.length; i++) {
                    final String curServer = providers[i];
                    final Set curSearchResults = jahiaUserManagerService.
                        searchUsers(curServer, siteID, searchParameters);
                    if (curSearchResults != null) {
                        searchResults.addAll(curSearchResults);
                    }
                }
            }
        }
        return searchResults;
    }


    /**
     * Get the group search result from the parameter form given by the request.
     * If the form is not in the request then all the Jahia groups will be search.
     *
     * @param processingContext the context that should contain the HTML formular with the
     * following fields :
     * - searchString
     * - searchIn
     * - properties
     * - storedOn
     * - providers
     * @param siteID The site ID containing the principal to search
     * @return a Properties object that contain the search criterium
     */
    public static Set getGroupSearchResult(ProcessingContext processingContext, int siteID) {

        String searchString = processingContext.getParameter("searchString");
        String searchIn = processingContext.getParameter("searchIn");
        String[] searchInProps = processingContext.getParameterValues("properties");
        String storedOn = processingContext.getParameter("storedOn");
        String[] providers = processingContext.getParameterValues("providers");

        return getGroupSearchResult(searchIn, siteID, searchString, searchInProps, storedOn, providers);
    }

    /**
     * Get the group search result from the parameter form given by the request.
     * If the form is not in the request then all the Jahia groups will be search.
     *
     * @param request the request that should contain the HTML formular with the
     * following fields :
     * - searchString
     * - searchIn
     * - properties
     * - storedOn
     * - providers
     * @param siteID The site ID containing the principal to search
     * @return a Properties object that contain the search criterium
     */
    public static Set getGroupSearchResult(ServletRequest request, int siteID) {

        String searchString = request.getParameter("searchString");
        final String searchIn = request.getParameter("searchIn");
        final String[] searchInProps = request.getParameterValues("properties");
        final String storedOn = request.getParameter("storedOn");
        final String[] providers = request.getParameterValues("providers");

        return getGroupSearchResult(searchIn, siteID, searchString, searchInProps, storedOn, providers);
    }

    public static Set getGroupSearchResult(String searchIn, int siteID, String searchString, String[] searchInProps, String storedOn, String[] providers) {
        JahiaGroupManagerService jahiaGroupManagerService =
            ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final Properties searchParameters = new Properties();
        final Set searchResults = new HashSet();
        if (searchIn == null) { // Necessary condition to say there is no formular.
            logger.debug("No formular transmited. Finding all Jahia DB users.");
            searchParameters.setProperty("*", "*");
            searchResults.addAll(jahiaGroupManagerService.
                searchGroups(siteID, searchParameters));
        } else {
            //if (searchString == null || "".equals(searchString)) {
            if ("".equals(searchString)) {
                searchString = "*";
            }
            if ("allProps".equals(searchIn) || searchInProps == null) {
                searchParameters.setProperty("*", searchString);
            } else {
                for (int i = 0; i < searchInProps.length; i++) {
                    searchParameters.setProperty(searchInProps[i], searchString);
                }
            }
            if ("everywhere".equals(storedOn) || providers == null) {
                searchResults.addAll(jahiaGroupManagerService.
                    searchGroups(siteID, searchParameters));
            } else {
                for (int i = 0; i < providers.length; i++) {
                    final String curServer = providers[i];
                    final Set curSearchResults = jahiaGroupManagerService.
                        searchGroups(curServer, siteID, searchParameters);
                    if (curSearchResults != null) {
                        searchResults.addAll(curSearchResults);
                    }
                }
            }
        }
        return searchResults;
    }

    /**
     * Utility method to remove from a set of users all the members of the
     * Jahia Administrators group. This is used when we don't want to display
     * this set of users.
     * @param users a set of users that we will remove the Jahia administrators
     * from
     * @return a set of users without the Jahia Administrators
     */
    public static Set removeJahiaAdministrators(Set users) {
        final Set usersWithoutJahiaAdmin = new HashSet(users);
        final JahiaGroup jahiaAdminGroup = ServicesRegistry.getInstance().
                getJahiaGroupManagerService().getAdministratorGroup(0);
        final Iterator memberEnum = new EnumerationIterator(jahiaAdminGroup.members());
        while (memberEnum.hasNext()) {
            final Object curMemberObject = memberEnum.next();
            if (curMemberObject instanceof JahiaUser) {
                usersWithoutJahiaAdmin.remove(curMemberObject);
            }
        }
        return usersWithoutJahiaAdmin;
    }

    public static Set getUserGroupMembership(JahiaUser usr, int site) {
        Set groups = new HashSet();
        JahiaGroupManagerService jahiaGroupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        List v = jahiaGroupManagerService.getUserMembership(usr);
        Iterator en = v.iterator();
        while (en.hasNext()) {
            String gname = (String) en.next();
            JahiaGroup g = jahiaGroupManagerService.lookupGroup(gname);
            if (g != null && (g.getSiteID() == site || g.getSiteID() ==0)) {
                groups.add(g);
            }
        }
        return groups;
    }
}
