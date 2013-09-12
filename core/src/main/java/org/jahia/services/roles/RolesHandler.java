package org.jahia.services.roles;

import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
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

public class RolesHandler implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RolesHandler.class);


    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

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

    public Map<String,List<Principal>> getRoles() throws Exception {
        Map<String,List<Principal>> m = new LinkedHashMap<String, List<Principal>>();

        final JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession();
        if (roles == null) {
            QueryManager qm = s.getWorkspace().getQueryManager();
            Query q = qm.createQuery("select * from [jnt:role] where [j:roleGroup]='" + roleGroup + "'", Query.JCR_SQL2);
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                m.put(next.getName(), new ArrayList<Principal>());
            }
        } else {
            for (String r : roles) {
                m.put(r, new ArrayList<Principal>());
            }
        }

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
            for (String[] strings : entry.getValue()) {
                String role = strings[2];

                if (strings[1].equals("GRANT") && m.containsKey(role)) {
                    m.get(role).add(p);
                }
            }
        }

        return m;
    }

    public void setRoles(JCRNodeWrapper node) throws RepositoryException {
        if (node.hasProperty("roles")) {
            roles = new ArrayList<String>();
            for (Value value : node.getProperty("roles").getValues()) {
                roles.add(value.getString());
            }
        } else {
            roles = null;
        }
    }

    public List<Principal> getRoleMembers() throws Exception{
        return getRoles().get(role);
    }

    public void grantRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        for (String principal : principals) {
            session.getNode(nodePath).grantRoles(principal, Collections.singleton(role));
        }
        session.save();
    }

    public void revokeRole(String[] principals, MessageContext messageContext) throws Exception {
        if (principals.length == 0) {
            return;
        }

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        Map<String,String> roles = new HashMap<String, String>();
        for (String principal : principals) {
            List<String[]> entries = session.getNode(nodePath).getAclEntries().get(principal);
            for (String[] strings : entries) {
                roles.put(strings[2], strings[1]);
            }
            roles.remove(role);
            session.getNode(nodePath).revokeRolesForPrincipal(principal);
            session.getNode(nodePath).changeRoles(principal, roles);
        }

        session.save();
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
     * @param searchCriteria
     *            current search criteria
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
            final JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession();
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
