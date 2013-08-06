package org.jahia.modules.rolesmanager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    enum Scope {CONTENT, SITE, SERVER_SETTINGS, STUDIO, JCR, OTHER};


    @Autowired
    private transient RoleTypeConfiguration roleTypes;

    private RoleBean roleBean = new RoleBean();
    private String currentTab;

    private transient Map<String,List<JCRNodeWrapper>> allPermissions;

    public RolesAndPermissionsHandler() {

//        defaultTabsForRoleByScope = new HashMap<RoleType, List<String>>();
//        defaultTabsForRoleByScope.put(RoleType.SERVER_ROLE, Arrays.asList("scope." + Scope.SERVER_SETTINGS.name(), "scope." + Scope.SITE.name(), "scope." + Scope.CONTENT.name(), "scope." + Scope.JCR.name()));
//        defaultTabsForRoleByScope.put(RoleType.SITE_ROLE, Arrays.asList("scope." + Scope.SITE.name(), "scope." + Scope.CONTENT.name(), "scope." + Scope.JCR.name()));
//        defaultTabsForRoleByScope.put(RoleType.EDIT_ROLE, Arrays.asList("scope." + Scope.CONTENT.name(), "scope." + Scope.JCR.name(), "context.$currentSite"));
//        defaultTabsForRoleByScope.put(RoleType.LIVE_ROLE, Arrays.asList("scope." + Scope.CONTENT.name(), "scope." + Scope.JCR.name()));

    }

    public RoleTypeConfiguration getRoleTypes() {
        return roleTypes;
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
        for (RoleType roleType : roleTypes.getValues()) {
            all.put(roleType.getName(), new ArrayList<RoleBean>());
        }

        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            RoleBean role = getRole(next.getIdentifier());
            String key = role.getRoleType().getName();
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
        String roleGroup = role.getProperty("j:roleGroup").getString();

        RoleType roleType = roleTypes.get(roleGroup);
        roleBean.setRoleType(roleType);

        List<String> setPermIds = new ArrayList<String>();
        if (role.hasProperty("j:permissions")) {
            Value[] values = role.getProperty("j:permissions").getValues();
            for (Value value : values) {
                setPermIds.add(value.getString());
            }
        }

        List<String> tabs = new ArrayList<String>(roleBean.getRoleType().getScopes());

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

        for (String tab : tabs) {
            addPermissionsForTab(permsForRole, tab, setPermIds, setExternalPermIds, roleBean.getRoleType().isPrivileged());
        }

        roleBean.setPermissions(permsForRole);

        return roleBean;
    }

    public RoleBean addRole(String roleName, String scope) throws RepositoryException {
        JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper role = currentUserSession.getNode("/roles").addNode(roleName, "jnt:role");
        RoleType roleType = roleTypes.get(scope);
        role.setProperty("j:roleGroup", roleType.getName());
        role.setProperty("j:privilegedAccess", roleType.isPrivileged());
        if (roleType.getNodeType() != null) {
            role.setProperty("j:nodeTypes", new Value[]{currentUserSession.getValueFactory().createValue(roleType.getNodeType())});
        }
        role.setProperty("j:roleGroup", roleType.getName());

        currentUserSession.save();
        return getRole(role.getIdentifier());
    }

    private void addPermissionsForTab(Map<String, Map<String, PermissionBean>> permsForRole, String tab, List<String> setPermIds, Map<String, List<String>> setExternalPermIds, boolean isPrivileged) throws RepositoryException {
        if (!permsForRole.containsKey(tab)) {
            permsForRole.put(tab, new TreeMap<String, PermissionBean>());
        }
        Map<String, List<JCRNodeWrapper>> allPermissions = getPermissions();
        if (tab.startsWith("scope.")) {
            List<JCRNodeWrapper> perms = new ArrayList<JCRNodeWrapper>(allPermissions.get(StringUtils.substringAfter(tab, "scope.")));

            for (JCRNodeWrapper permissionNode : perms) {
                if (!permissionNode.hasProperty("j:requirePrivileged") || permissionNode.getProperty("j:requirePrivileged").getBoolean() == isPrivileged) {
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
            }
        } else if (tab.startsWith("context.")) {
            String context = StringUtils.substringAfter(tab, "context.");
            List<JCRNodeWrapper> perms;

            if (context.equals("$currentSite")) {
                perms = new ArrayList<JCRNodeWrapper>(allPermissions.get(Scope.SITE.name()));
//                perms.addAll(allPermissions.get(Scope.CONTENT.name()));
            } else {
                perms = new ArrayList<JCRNodeWrapper>(allPermissions.get(Scope.JCR.name()));
                try {
                    JCRNodeWrapper contextNode = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(context);
                    String ntname = contextNode.getPrimaryNodeTypeName();
                    Scope scope = getScopeForType(ntname);
                    if (!scope.equals(Scope.OTHER)) {
                        perms.addAll(allPermissions.get(scope.name()));
                    }

                } catch (RepositoryException e) {
                    e.printStackTrace();
                }

            }
            for (JCRNodeWrapper permissionNode : perms) {
                if (!permissionNode.hasProperty("j:requirePrivileged") || permissionNode.getProperty("j:requirePrivileged").getBoolean() == isPrivileged) {
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
            }
//        } else if (tab.startsWith("special.")) {
//            String special = StringUtils.substringAfter(tab, "special.");

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

        if (node.getPath().startsWith("/permissions/repository-permissions")) {
            scope = Scope.JCR;
        }
        if (node.hasProperty("j:nodeTypes")) {
            Set<String> s = new HashSet<String>();
            Value[] values = node.getProperty("j:nodeTypes").getValues();
            for (Value value : values) {
                scope = getScopeForType(value.getString());
                if (scope != Scope.OTHER) {
                    return scope;
                }
            }

        }
        return scope;
    }

    private Scope getScopeForType(String s) throws RepositoryException {
        Scope scope = Scope.OTHER;
        if (s.equals("jnt:globalSettings")) {
            scope = Scope.SERVER_SETTINGS;
        } else if (s.equals("jnt:virtualsite")) {
            scope = Scope.SITE;
        } else if (s.equals("jnt:modules")) {
            scope = Scope.STUDIO;
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
            addPermissionsForTab(roleBean.getPermissions(), tab, new ArrayList<String>(), new HashMap<String, List<String>>(), roleBean.getRoleType().isPrivileged());
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
//            if (entry.getKey().startsWith("special.")) {
//
//            }
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

    public Map<String, List<JCRNodeWrapper>> getPermissions() throws RepositoryException {
        if (allPermissions != null) {
            return allPermissions;
        }

        allPermissions = new LinkedHashMap<String, List<JCRNodeWrapper>>();
        JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();

        for (Scope scope : Scope.values()) {
            allPermissions.put(scope.name(), new ArrayList<JCRNodeWrapper>());
        }


        QueryManager qm = currentUserSession.getWorkspace().getQueryManager();
        String statement = "select * from [jnt:permission]";

        Query q = qm.createQuery(statement, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.getDepth() > 1) {
                Scope scope = getScope((JCRNodeWrapper)next.getAncestor(2));
                allPermissions.get(scope.name()).add(next);
            }
        }
        for (List<JCRNodeWrapper> list : allPermissions.values()) {
            Collections.sort(list, new Comparator<JCRNodeWrapper>() {
                @Override
                public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
        }

        return allPermissions;
    }

}
