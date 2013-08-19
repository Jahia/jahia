package org.jahia.modules.serversettings.flow;

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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

public class ServerRolesHandler implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ServerRolesHandler.class);


    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    private String roleGroup = "server-role";

    private String searchType = "users";

    private String role;

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

    public Map<String,List<Principal>> getServerRoles() throws Exception {
        Map<String,List<Principal>> m = new HashMap<String,List<Principal>>();

        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery("select * from [jnt:role] where [j:roleGroup]='" + roleGroup + "'", Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            m.put(next.getName(), new ArrayList<Principal>());
        }

        JCRSessionWrapper s = session;

        Map<String, List<String[]>> acl = s.getNode("/").getAclEntries();

        for (Map.Entry<String, List<String[]>> entry : acl.entrySet()) {
            Principal p;
            if (entry.getKey().startsWith("u:")) {
                p = userManagerService.lookupUser(entry.getKey().substring(2));
            } else if (entry.getKey().startsWith("g:")) {
                p = groupManagerService.lookupGroup(null,entry.getKey().substring(2));
            } else {
                continue;
            }
            for (String[] strings : entry.getValue()) {
                String role = strings[2];

                if (!m.containsKey(role)) {
                    m.put(role, new ArrayList<Principal>());
                }
                m.get(role).add(p);
            }
        }

        return m;
    }

    public List<Principal> getRoleMembers() throws Exception{
        return getServerRoles().get(role);
    }

    public void grantRole(String[] principals, MessageContext messageContext) throws Exception {
        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        for (String principal : principals) {
            session.getNode("/").grantRoles(principal, Collections.singleton(role));
        }
        session.save();
    }

    public void revokeRole(String[] principals, MessageContext messageContext) throws Exception {
        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        Map<String,String> roles = new HashMap<String, String>();
        for (String principal : principals) {
            List<String[]> entries = session.getNode("/").getAclEntries().get(principal);
            for (String[] strings : entries) {
                roles.put(strings[2], strings[1]);
            }
            roles.remove(role);
            session.getNode("/").revokeRolesForPrincipal(principal);
            session.getNode("/").changeRoles(principal, roles);
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
    public Set<Principal> searchNewMembers(SearchCriteria searchCriteria) {
        long timer = System.currentTimeMillis();

        Set<Principal> searchResult;
        if (searchType.equals("users")) {
            searchResult = PrincipalViewHelper.getSearchResult(searchCriteria.getSearchIn(),
                    searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                    searchCriteria.getProviders());
        } else {
            searchResult = PrincipalViewHelper.getGroupSearchResult(searchCriteria.getSearchIn(),
                    searchCriteria.getSiteId(), searchCriteria.getSearchString(), searchCriteria.getProperties(),
                    searchCriteria.getStoredOn(), searchCriteria.getProviders());
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
