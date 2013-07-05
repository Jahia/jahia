package org.jahia.modules.rolesmanager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.util.*;

public class RolesAndPermissionsHandler implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(RolesAndPermissionsHandler.class);

    enum Scope {CONTENT, SITE, SERVER, OTHER}

    ;

    private RoleBean roleBean = new RoleBean();
    private String currentTab;

    private Map<Scope, List<String>> defaultTabsForRoleByScope;
//    private Map<String, String> tabs;

    public RolesAndPermissionsHandler() {
        defaultTabsForRoleByScope = new HashMap<Scope, List<String>>();
        defaultTabsForRoleByScope.put(Scope.SERVER, Arrays.asList("scope." + Scope.SERVER.name(), "scope." + Scope.SITE.name(), "scope." + Scope.CONTENT.name()));
        defaultTabsForRoleByScope.put(Scope.SITE, Arrays.asList("scope." + Scope.SITE.name(), "scope." + Scope.CONTENT.name()));
        defaultTabsForRoleByScope.put(Scope.CONTENT, Arrays.asList("scope." + Scope.CONTENT.name(), "context.$currentSite"));

//        tabs = new HashMap<String, String>();
//        tabs.put("action", "action");
//        tabs.put("admin", "site-admin");
//        tabs.put("contributeMode", "ui");
//        tabs.put("editMode", "ui");
//        tabs.put("managers", "managers");
//        tabs.put("repository-permissions", "basic");
//        tabs.put("studioMode", "ui");
//        tabs.put("workflow-tasks", "workflow");
//        tabs.put("wysiwyg-editor-toolbar", "ui");
//        tabs.put("jobs", "ui");
    }

    public RoleBean getRoleBean() {
        return roleBean;
    }

    public void setRoleBean(RoleBean roleBean) {
        this.roleBean = roleBean;
        this.currentTab = roleBean.getPermissions().keySet().iterator().next();
    }

    public Map<String, List<RoleBean>> getRoles() throws RepositoryException {

        QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
        Query q = qm.createQuery("select * from [jnt:role]", Query.JCR_SQL2);
        Map<String, List<RoleBean>> all = new LinkedHashMap<String, List<RoleBean>>();
        all.put(Scope.CONTENT.name()+ " / LIVE ROLE", new ArrayList<RoleBean>());
        all.put(Scope.CONTENT.name()+ " ROLE", new ArrayList<RoleBean>());
        all.put(Scope.SITE.name()+ " ROLE", new ArrayList<RoleBean>());
        all.put(Scope.SERVER.name()+ " ROLE", new ArrayList<RoleBean>());

        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            RoleBean role = getRole(next.getIdentifier());
            String key = role.getScope().name() + (role.isPrivileged() ? "" : " / LIVE") + " ROLE";
            if (!all.containsKey(key)) {
                all.put(key, new ArrayList<RoleBean>());
            }
            all.get(key).add(role);
        }
        for (List<RoleBean> roleBeans : all.values()) {
            Collections.sort(roleBeans, new Comparator<RoleBean>() {
                @Override
                public int compare(RoleBean o1, RoleBean o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }

        return all;
    }

    public RoleBean getRole(String uuid) throws RepositoryException {
        JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRNodeWrapper role = currentUserSession.getNodeByIdentifier(uuid);

        RoleBean roleBean = new RoleBean();
        roleBean.setUuid(uuid);
        roleBean.setName(role.getName());
        roleBean.setDepth(role.getDepth());
        Scope scope = getScope(role);

        roleBean.setScope(scope);
        if (role.hasProperty("j:privilegedAccess") && role.getProperty("j:privilegedAccess").getBoolean()) {
            roleBean.setPrivileged(true);
        } else {
            roleBean.setPrivileged(false);
        }

        List<String> setPermIds = new ArrayList<String>();
        if (role.hasProperty("j:permissions")) {
            Value[] values = role.getProperty("j:permissions").getValues();
            for (Value value : values) {
                setPermIds.add(value.getString());
            }
        }

        List<String> tabs = new ArrayList<String>(defaultTabsForRoleByScope.get(scope));

        Map<String, List<String>> setExternalPermIds = new HashMap<String, List<String>>();
        NodeIterator ni = role.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.isNodeType("jnt:externalPermissions")) {
                try {
                    String path = next.getProperty("j:path").getString();
                    setExternalPermIds.put(path, new ArrayList<String>());
                    Value[] values = next.getProperty("j:permissions").getValues();
                    for (Value value : values) {
                        setExternalPermIds.get(path).add(value.getString());
                        if (!tabs.contains("context." + path)) {
                            tabs.add("context." + path);
                        }
                    }
                } catch (RepositoryException e) {
                    System.out.println(next.getPath());
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IllegalStateException e) {
                    System.out.println(next.getPath());
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        Map<String, Map<String, PermissionBean>> permsForRole = new LinkedHashMap<String, Map<String, PermissionBean>>();

        Map<String, List<JCRNodeWrapper>> p = getPermissions(roleBean);

        for (String tab : tabs) {
            addPermissionsForTab(permsForRole, p, tab, setPermIds, setExternalPermIds);
        }

//        for (String s : p.keySet()) {
//            for (JCRNodeWrapper permissionNode : p.get(s)) {
//                Scope permissionScope = getScope(permissionNode);
//                if (permissionScope == Scope.OTHER) {
//                    Set<String> nodeTypes = new HashSet<String>();
//                    Value[] values = permissionNode.getProperty("j:nodeTypes").getValues();
//                    for (Value value : values) {
//                        nodeTypes.add(value.getString());
//                    }
//                    for (String nodeType : nodeTypes) {
//                        String tab = "Special : " +nodeType;
////                        if (g.get(scope).contains(tab)) {
//                            if (!extPermsForRole.containsKey(tab)) {
//                                extPermsForRole.put(tab, new ArrayList<PermissionBean>());
//                            }
//                            ExternalPermissionBean bean = new ExternalPermissionBean();
//                            bean.setName(permissionNode.getName());
//                            bean.setPath(permissionNode.getPath());
//                            bean.setDepth(permissionNode.getDepth());
//                            bean.setTargetPaths(new ArrayList<String>());
//                            bean.setSetForPath(new HashMap<String, Boolean>());
//                            bean.setScope(permissionScope);
//                            extPermsForRole.get(tab).add(bean);
//                            Collections.sort(extPermsForRole.get(tab));
//
//                            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery("select * from [" + nodeType + "]", Query.JCR_SQL2);
//                            NodeIterator ni2 = q.execute().getNodes();
//                            while (ni2.hasNext()) {
//                                JCRNodeWrapper next = (JCRNodeWrapper) ni2.next();
//                                bean.getTargetPaths().add(next.getPath());
//                                if (extPermIds.get(next.getPath()) != null && extPermIds.get(next.getPath()).contains(permissionNode.getIdentifier())) {
//                                    bean.getSetForPath().put(next.getPath(), true);
//                                }
//                            }
////                        }
//                    }
//                } else if (scope.ordinal() >= permissionScope.ordinal()) {
//                    String tab = permissionScope.name();
////                    if (g.get(scope).contains(tab)) {
//                        if (!permsForRole.containsKey(tab)) {
//                            permsForRole.put(tab, new ArrayList<PermissionBean>());
//                        }
//                        PermissionBean bean = new PermissionBean();
//                        bean.setName(permissionNode.getName());
//                        bean.setPath(permissionNode.getPath());
//                        bean.setDepth(permissionNode.getDepth());
//                        bean.setScope(permissionScope);
//                        if (permIds.contains(permissionNode.getIdentifier())) {
//                            bean.setSet(true);
//                        }
//                        permsForRole.get(tab).add(bean);
//                        Collections.sort(permsForRole.get(tab));
////                    }
//                }
//            }
//        }
//

        roleBean.setPermissions(permsForRole);

//        roleBean.setExternalPermissions(extPermsForRole);

        return roleBean;
    }

    public RoleBean addRole(String roleName, String scope) throws RepositoryException {
        JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper role = currentUserSession.getNode("/roles").addNode(roleName, "jnt:role");
        switch (Scope.valueOf(StringUtils.substringBefore(scope, "-"))) {
            case SITE:
                role.setProperty("j:nodeTypes", new Value[]{currentUserSession.getValueFactory().createValue("jnt:virtualsite")});
                break;
            case SERVER:
                role.setProperty("j:nodeTypes", new Value[]{currentUserSession.getValueFactory().createValue("rep:root")});
                break;
        }


        if (Boolean.valueOf(StringUtils.substringAfter(scope, "-"))) {
            role.setProperty("j:privilegedAccess", true);
            role.setProperty("j:roleGroup", "edit-role");
        } else {
            role.setProperty("j:roleGroup", "live-role");
        }

        currentUserSession.save();
        return getRole(role.getIdentifier());
    }

    private void addPermissionsForTab(Map<String, Map<String, PermissionBean>> permsForRole, Map<String, List<JCRNodeWrapper>> allPermissions, String tab, List<String> setPermIds, Map<String, List<String>> setExternalPermIds) throws RepositoryException {
        if (!permsForRole.containsKey(tab)) {
            permsForRole.put(tab, new TreeMap<String, PermissionBean>());
        }
        if (tab.startsWith("scope.")) {
            List<JCRNodeWrapper> perms = allPermissions.get(StringUtils.substringAfter(tab, "scope."));
            for (JCRNodeWrapper permissionNode : perms) {
                PermissionBean bean = new PermissionBean();
                bean.setUuid(permissionNode.getIdentifier());
                bean.setParentPath(permissionNode.getParent().getPath());
                bean.setName(permissionNode.getName());
                bean.setPath(permissionNode.getPath());
                bean.setDepth(permissionNode.getDepth());
                bean.setScope(getScope(permissionNode));
                PermissionBean parentBean = permsForRole.get(tab).get(bean.getParentPath());
                if (setPermIds.contains(permissionNode.getIdentifier()) || (parentBean != null && parentBean.isSet())) {
                    bean.setSet(true);
                    while (parentBean != null && !parentBean.isSet()) {
                        parentBean.setPartialSet(true);
                        parentBean = permsForRole.get(tab).get(parentBean.getParentPath());
                    }
                }
                permsForRole.get(tab).put(permissionNode.getPath(), bean);
            }
        } else if (tab.startsWith("context.")) {
            String context = StringUtils.substringAfter(tab, "context.");
            List<JCRNodeWrapper> perms;

            if (context.equals("$currentSite")) {
                perms = new ArrayList<JCRNodeWrapper>(allPermissions.get(Scope.SITE.name()));
                perms.addAll(allPermissions.get(Scope.CONTENT.name()));
            } else {
                try {
                    JCRNodeWrapper contextNode = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(context);

                } catch (RepositoryException e) {
                    e.printStackTrace();
                }

                perms = allPermissions.get(Scope.CONTENT.name());
            }
            for (JCRNodeWrapper permissionNode : perms) {
                ExternalPermissionBean bean = new ExternalPermissionBean();
                bean.setUuid(permissionNode.getIdentifier());
                bean.setParentPath(permissionNode.getParent().getPath());
                bean.setName(permissionNode.getName());
                bean.setPath(permissionNode.getPath());
                bean.setDepth(permissionNode.getDepth());
                bean.setTargetPath(context);
                bean.setScope(getScope(permissionNode));
                PermissionBean parentBean = permsForRole.get(tab).get(bean.getParentPath());
                if ((setExternalPermIds.get(context) != null && setExternalPermIds.get(context).contains(permissionNode.getIdentifier()))
                        || (parentBean != null && parentBean.isSet())) {
                    bean.setSet(true);
                    while (parentBean != null && !parentBean.isSet()) {
                        parentBean.setPartialSet(true);
                        parentBean = permsForRole.get(tab).get(parentBean.getParentPath());
                    }
                }
                permsForRole.get(tab).put(permissionNode.getPath(), bean);
            }
        } else if (tab.startsWith("special.")) {
            String special = StringUtils.substringAfter(tab, "special.");

        }
    }

    public String getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(String tab) {
        currentTab = tab;
    }

    private Scope getScope(JCRNodeWrapper node) throws RepositoryException {
        Scope scope = Scope.CONTENT;

        if (node.hasProperty("j:nodeTypes")) {
            Set<String> s = new HashSet<String>();
            Value[] values = node.getProperty("j:nodeTypes").getValues();
            for (Value value : values) {
                s.add(value.getString());
            }

            if (s.contains("rep:root")) {
                scope = Scope.SERVER;
            } else if (s.contains("jnt:virtualsite")) {
                scope = Scope.SITE;
            } else {
                scope = Scope.OTHER;
            }
        }
        return scope;
    }

    public void storeValues(String[] selectedValues, String[] partialSelectedValues) {
        Map<String, PermissionBean> permissionBeans = roleBean.getPermissions().get(currentTab);
        List<String> perms = selectedValues != null ? Arrays.asList(selectedValues) : new ArrayList<String>();
        for (PermissionBean permissionBean : permissionBeans.values()) {
            permissionBean.setSet(perms.contains(permissionBean.getPath()));
        }

        perms = partialSelectedValues != null ? Arrays.asList(partialSelectedValues) : new ArrayList<String>();
        for (PermissionBean permissionBean : permissionBeans.values()) {
            permissionBean.setPartialSet(perms.contains(permissionBean.getPath()));
        }
    }

    public void addContext(String newContext) throws RepositoryException {
        if (!newContext.startsWith("/")) {
            return;
        }

        String tab = "context." + newContext;
        if (!roleBean.getPermissions().containsKey(tab)) {
            Map<String, List<JCRNodeWrapper>> p = getPermissions(roleBean);
            addPermissionsForTab(roleBean.getPermissions(), p, tab, new ArrayList<String>(), new HashMap<String, List<String>>());
        }
        setCurrentTab(tab);
    }

    public void save() throws RepositoryException {
        JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();

        List<Value> permissionsValues = new ArrayList<Value>();
        Map<String, List<Value>> externalPermissions = new HashMap<String, List<Value>>();

        for (Map.Entry<String, Map<String, PermissionBean>> entry : roleBean.getPermissions().entrySet()) {
            if (entry.getKey().startsWith("scope.")) {
                for (PermissionBean bean : entry.getValue().values()) {
                    PermissionBean parentBean = entry.getValue().get(bean.getParentPath());
                    if (bean.isSet() && (parentBean == null || !parentBean.isSet())) {
                        permissionsValues.add(currentUserSession.getValueFactory().createValue(bean.getUuid(), PropertyType.WEAKREFERENCE));
                    }
                }
            }
            if (entry.getKey().startsWith("context.")) {
                String path = StringUtils.substringAfter(entry.getKey(), "context.");
                ArrayList<Value> values = new ArrayList<Value>();
                externalPermissions.put(path, values);
                for (PermissionBean bean : entry.getValue().values()) {
                    PermissionBean parentBean = entry.getValue().get(bean.getParentPath());
                    if (bean.isSet() && (parentBean == null || !parentBean.isSet())) {
                        values.add(currentUserSession.getValueFactory().createValue(bean.getUuid(), PropertyType.WEAKREFERENCE));
                    }
                }
            }
            if (entry.getKey().startsWith("special.")) {

            }
        }

        JCRNodeWrapper role = currentUserSession.getNodeByIdentifier(roleBean.getUuid());
        role.setProperty("j:permissions", permissionsValues.toArray(new Value[permissionsValues.size()]));
        for (Map.Entry<String, List<Value>> s : externalPermissions.entrySet()) {
            String key = s.getKey();
            if (key.equals("/")) {
                key = "root-access";
            } else {
                key = ISO9075.encode(key.substring(1).replace("/", "-")) + "-access";
            }
            if (!role.hasNode(key)) {
                JCRNodeWrapper extPermissions = role.addNode(key, "jnt:externalPermissions");
                extPermissions.setProperty("j:path", s.getKey());
                extPermissions.setProperty("j:permissions", s.getValue().toArray(new Value[s.getValue().size()]));
            } else {
                role.getNode(key).setProperty("j:permissions", s.getValue().toArray(new Value[s.getValue().size()]));
            }

        }

        currentUserSession.save();
    }

    public Map<String, List<JCRNodeWrapper>> getPermissions(RoleBean roleBean) throws RepositoryException {
        Map<String, List<JCRNodeWrapper>> m = new LinkedHashMap<String, List<JCRNodeWrapper>>();
        JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();

        for (Scope scope : Scope.values()) {
            m.put(scope.name(), new ArrayList<JCRNodeWrapper>());
        }


        QueryManager qm = currentUserSession.getWorkspace().getQueryManager();
        String statement = "select * from [jnt:permission]";
        if (roleBean.isPrivileged()) {
//            statement += " where [j:requirePrivileged]=true";
        } else {
            statement += " where [j:requirePrivileged]=false or [j:requirePrivileged] is null";
        }
        Query q = qm.createQuery(statement, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            Scope scope = getScope(next);
            if (next.getDepth() > 1) {
                m.get(scope.name()).add(next);
            }
        }
        for (List<JCRNodeWrapper> list : m.values()) {
            Collections.sort(list, new Comparator<JCRNodeWrapper>() {
                @Override
                public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
        }

        return m;
    }

}
