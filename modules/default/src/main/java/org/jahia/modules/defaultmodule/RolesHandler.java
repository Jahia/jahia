/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.modules.defaultmodule;

import org.jahia.ajax.gwt.helper.PublicationHelper;
import org.jahia.api.Constants;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroupManagerProvider;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.SearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

import static org.jahia.api.Constants.EDIT_WORKSPACE;
import static org.jahia.api.Constants.LIVE_WORKSPACE;

public class RolesHandler implements Serializable {
    private static final long serialVersionUID = 2485636561921483297L;


    private static final Logger logger = LoggerFactory.getLogger(RolesHandler.class);


    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    @Autowired
    private transient JCRPublicationService publicationService;

    private String workspace;

    private Locale locale;

    private Locale fallbackLocale;

    private String roleGroup;

    private String searchType = "users";

    private String role;

    private String nodePath;

    private List<String> roles;

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public Map<JCRNodeWrapper, List<Principal>> getRoles() throws Exception {
        Map<String, JCRNodeWrapper> rolesFromName = new HashMap<String, JCRNodeWrapper>();
        Map<JCRNodeWrapper, List<Principal>> result = new TreeMap<JCRNodeWrapper, List<Principal>>(new Comparator<JCRNodeWrapper>() {
            @Override
            public int compare(JCRNodeWrapper jcrNodeWrapper, JCRNodeWrapper jcrNodeWrapper2) {
                return jcrNodeWrapper.getDisplayableName().compareTo(jcrNodeWrapper2.getDisplayableName());
            }
        });
        final JCRSessionWrapper defaultSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, locale, fallbackLocale);
        QueryManager qm = defaultSession.getWorkspace().getQueryManager();
        if (role != null) {
            Query q = qm.createQuery("select * from [jnt:role] where localname()='" + role + "'", Query.JCR_SQL2);
            getRoles(q, rolesFromName, result);
        } else if (roles == null) {
            Query q = qm.createQuery("select * from [jnt:role] where [j:roleGroup]='" + roleGroup + "'", Query.JCR_SQL2);
            getRoles(q, rolesFromName, result);
        } else {
            for (String r : roles) {
                Query q = qm.createQuery("select * from [jnt:role] where localname()='" + r + "'", Query.JCR_SQL2);
                getRoles(q, rolesFromName, result);
            }
        }

        final JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);
        JCRNodeWrapper node = s.getNode(nodePath);
        Map<String, List<String[]>> acl = node.getAclEntries();

        for (Map.Entry<String, List<String[]>> entry : acl.entrySet()) {
            Principal p;
            if (entry.getKey().startsWith("u:")) {
                p = userManagerService.lookupUser(entry.getKey().substring(2));
            } else if (entry.getKey().startsWith("g:")) {
                p = groupManagerService.lookupGroup(0, entry.getKey().substring(2));
                if (p == null && nodePath.startsWith("/sites/")) {
                    int siteID = node.getResolveSite().getID();
                    p = groupManagerService.lookupGroup(siteID, entry.getKey().substring(2));
                }
                if (p == null) {
                    continue;
                }
            } else {
                continue;
            }
            final List<String[]> value = entry.getValue();
            Collections.reverse(value);
            for (String[] strings : value) {
                String role = strings[2];

                if (strings[1].equals("GRANT") && rolesFromName.containsKey(role) && !result.get(rolesFromName.get(role)).contains(p)) {
                    result.get(rolesFromName.get(role)).add(p);
                } else if (strings[1].equals("DENY") && rolesFromName.containsKey(role)) {
                    result.get(rolesFromName.get(role)).remove(p);
                }
            }
        }

        return result;
    }

    private void getRoles(Query q, Map<String, JCRNodeWrapper> rolesFromName, Map<JCRNodeWrapper, List<Principal>> m) throws RepositoryException {
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            m.put(next, new ArrayList<Principal>());
            rolesFromName.put(next.getName(), next);
        }
    }

    public void setContext(JCRNodeWrapper node, RenderContext context) throws RepositoryException {
        if (node.hasProperty("roles")) {
            roles = new ArrayList<String>();
            for (Value value : node.getProperty("roles").getValues()) {
                roles.add(value.getString());
            }
        } else {
            roles = null;
        }
        if (node.hasProperty("roleGroup")) {
            roleGroup = node.getProperty("roleGroup").getString();
        }
        if (node.hasProperty("contextNodePath")) {
            nodePath = node.getProperty("contextNodePath").getString();
        } else {
            nodePath = context.getMainResource().getNode().getPath();
        }
        workspace = node.getSession().getWorkspace().getName();
        locale = node.getSession().getLocale();
        fallbackLocale = node.getSession().getFallbackLocale();
    }

    public List<Principal> getRoleMembers() throws Exception {
        Map<JCRNodeWrapper, List<Principal>> r = getRoles();
        return r.size() > 0 ? r.entrySet().iterator().next().getValue() : new ArrayList<Principal>();
    }

    public void grantRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);
        for (String principal : principals) {
            session.getNode(nodePath).grantRoles(principal, Collections.singleton(role));
        }
        session.save();
        // Publish the node acls
        if (Constants.EDIT_WORKSPACE.equals(workspace) && session.getNode(nodePath).hasNode("j:acl")) {
            publicationService.publishByMainId(session.getNode(nodePath).getNode("j:acl").getIdentifier());
        }
    }

    public void revokeRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);

        Map<String, String> roles = new HashMap<String, String>();
        for (String principal : principals) {
            List<String[]> entries = session.getNode(nodePath).getAclEntries().get(principal);
            for (String[] strings : entries) {
                if (!role.equals(strings[2])) {
                    roles.put(strings[2], strings[1]);
                } else if (!strings[0].equals(nodePath)) {
                    roles.put(strings[2], "DENY");
                }
            }
            session.getNode(nodePath).revokeRolesForPrincipal(principal);
            session.getNode(nodePath).changeRoles(principal, roles);
        }

        session.save();
        // Publish the node acls
        if (Constants.EDIT_WORKSPACE.equals(workspace) && session.getNode(nodePath).hasNode("j:acl")) {
            publicationService.publishByMainId(session.getNode(nodePath).getNode("j:acl").getIdentifier());
        }
    }

    /**
     * Returns an empty (newly initialized) search criteria bean.
     *
     * @return an empty (newly initialized) search criteria bean
     */
    public SearchCriteria initCriteria(RequestContext ctx) {
        return new SearchCriteria(0);
    }


    public Map<String, ? extends JahiaGroupManagerProvider> getProviders() {
        Map<String, JahiaGroupManagerProvider> providers = new LinkedHashMap<String, JahiaGroupManagerProvider>();
        for (JahiaGroupManagerProvider p : groupManagerService.getProviderList()) {
            providers.put(p.getKey(), p);
        }
        return providers;
    }

    /**
     * Performs the group search with the specified search criteria and returns the list of matching groups.
     *
     * @param searchCriteria current search criteria
     * @return the list of groups, matching the specified search criteria
     */
    public Set<Principal> searchNewMembers(SearchCriteria searchCriteria) throws RepositoryException {
        long timer = System.currentTimeMillis();

        Set<Principal> searchResult;
        if (searchType.equals("users")) {
            searchResult = PrincipalViewHelper.getSearchResult(searchCriteria.getSearchIn(),
                    searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                    searchCriteria.getProviders());
        } else {
            final JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, fallbackLocale);
            searchResult = PrincipalViewHelper.getGroupSearchResult(searchCriteria.getSearchIn(), 0,
                    searchCriteria.getSearchString(), searchCriteria.getProperties(),
                    searchCriteria.getStoredOn(), searchCriteria.getProviders());
            if (nodePath.startsWith("/sites/")) {
                int siteID = s.getNode(nodePath).getResolveSite().getID();
                searchResult.addAll(PrincipalViewHelper.getGroupSearchResult(searchCriteria.getSearchIn(), siteID,
                        searchCriteria.getSearchString(), searchCriteria.getProperties(),
                        searchCriteria.getStoredOn(), searchCriteria.getProviders()));
            }
        }

        logger.info("Found {} groups in {} ms", searchResult.size(), System.currentTimeMillis() - timer);
        return searchResult;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchType() {
        return searchType;
    }

}
