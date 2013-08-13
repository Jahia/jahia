package org.jahia.modules.rolesmanager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;

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

//    enum Scope {CONTENT, SITE, SERVER_SETTINGS, STUDIO, JCR, OTHER};


    @Autowired
    private transient RoleTypeConfiguration roleTypes;

    private RoleBean roleBean = new RoleBean();

    private String currentContext;
    private String currentGroup;

    private boolean usePermissionMapping = true;

    private transient List<JCRNodeWrapper> allPermissions;

    public RolesAndPermissionsHandler() {
    }

    public RoleTypeConfiguration getRoleTypes() {
        return roleTypes;
    }

    public RoleBean getRoleBean() {
        return roleBean;
    }

    public void setRoleBean(RoleBean roleBean) {
        this.roleBean = roleBean;
        this.currentContext = "current";
        this.currentGroup = roleBean.getPermissions().get(currentContext).keySet().iterator().next();
    }

    private JCRSessionWrapper getSession() throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentUserSession("default", LocaleContextHolder.getLocale());
    }

    public Map<String, List<RoleBean>> getRoles() throws RepositoryException {

        QueryManager qm = getSession().getWorkspace().getQueryManager();
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
                    return o1.getPath().compareTo(o2.getPath());
                }
            });
        }

        return all;
    }

    public RoleBean getRole(String uuid) throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();

        JCRNodeWrapper role = currentUserSession.getNodeByIdentifier(uuid);

        RoleBean roleBean = new RoleBean();
        roleBean.setUuid(role.getIdentifier());
        roleBean.setName(role.getName());
        roleBean.setPath(role.getPath());
        roleBean.setDepth(role.getDepth());
        if (role.hasProperty("jcr:title")) {
            roleBean.setTitle(role.getProperty("jcr:title").getString());
        }
        if (role.hasProperty("jcr:description")) {
            roleBean.setDescription(role.getProperty("jcr:description").getString());
        }
        if (role.hasProperty("j:hidden")) {
            roleBean.setHidden(role.getProperty("j:hidden").getBoolean());
        }

        String roleGroup = role.getProperty("j:roleGroup").getString();

        RoleType roleType = roleTypes.get(roleGroup);
        roleBean.setRoleType(roleType);

        List<String> tabs = new ArrayList<String>(roleBean.getRoleType().getScopes());

        Map<String, List<String>> permIdsMap = new HashMap<String, List<String>>();
        fillPermIds(role, tabs, permIdsMap, false);

        Map<String, List<String>> inheritedPermIdsMap = new HashMap<String, List<String>>();
        fillPermIds(role.getParent(), tabs, inheritedPermIdsMap, true);


        Map<String, Map<String, Map<String, PermissionBean>>> permsForRole = new LinkedHashMap<String, Map<String, Map<String, PermissionBean>>>();
        roleBean.setPermissions(permsForRole);

        for (String tab : tabs) {
            long l = System.currentTimeMillis();
            addPermissionsForScope(roleBean, tab, permIdsMap, inheritedPermIdsMap);
            System.out.println(tab+ " fill perms = "+(System.currentTimeMillis() -l));
        }


        return roleBean;
    }

    private void fillPermIds(JCRNodeWrapper role, List<String> tabs, Map<String, List<String>> permIdsMap, boolean recursive) throws RepositoryException {
        if (!role.isNodeType(Constants.JAHIANT_ROLE)) {
            return;
        }

        if (recursive) {
            fillPermIds(role.getParent(), tabs, permIdsMap, true);
        }

        final ArrayList<String> setPermIds = new ArrayList<String>();
        permIdsMap.put("current", setPermIds);

        if (role.hasProperty("j:permissions")) {
            Value[] values = role.getProperty("j:permissions").getValues();
            for (Value value : values) {
                String valueString = value.getString();
                if (!setPermIds.contains(valueString)) {
                    setPermIds.add(valueString);
                }
            }
        }


        NodeIterator ni = role.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.isNodeType("jnt:externalPermissions")) {
                try {
                    String path = next.getProperty("j:path").getString();
                    permIdsMap.put(path, new ArrayList<String>());
                    Value[] values = next.getProperty("j:permissions").getValues();
                    for (Value value : values) {
                        List<String> ids = permIdsMap.get(path);
                        String valueString = value.getString();
                        if (!ids.contains(valueString)) {
                            ids.add(valueString);
                        }
                        if (!tabs.contains(path)) {
                            tabs.add(path);
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot initialize role " + next.getPath(), e);
                } catch (IllegalStateException e) {
                    logger.error("Cannot initialize role " + next.getPath(), e);
                }
            }
        }
    }

    public boolean addRole(String roleName, String parentRoleId, String roleTypeString, MessageContext messageContext) throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();

        if (StringUtils.isBlank(roleName)) {
            messageContext.addMessage(new MessageBuilder().source("roleName")
                    .defaultText(getMessage("rolesmanager.rolesAndPermissions.role.noName"))
                    .error()
                    .build());
            return false;
        }
        roleName = JCRContentUtils.generateNodeName(roleName);

        NodeIterator nodes = currentUserSession.getWorkspace().getQueryManager().createQuery(
                "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + roleName + "' and isdescendantnode(r,['/roles'])",
                Query.JCR_SQL2).execute().getNodes();
        if (nodes.hasNext()) {
            messageContext.addMessage(new MessageBuilder().source("roleName")
                    .defaultText(getMessage("rolesmanager.rolesAndPermissions.role.exists"))
                    .error()
                    .build());
            return false;
        }

        JCRNodeWrapper parent;
        if (StringUtils.isBlank(parentRoleId)) {
            parent = currentUserSession.getNode("/roles");
        } else {
            parent = currentUserSession.getNodeByIdentifier(parentRoleId);
        }
        JCRNodeWrapper role = parent.addNode(roleName, "jnt:role");
        RoleType roleType = roleTypes.get(roleTypeString);
        role.setProperty("j:roleGroup", roleType.getName());
        role.setProperty("j:privilegedAccess", roleType.isPrivileged());
        if (roleType.getNodeType() != null) {
            role.setProperty("j:nodeTypes", new Value[]{currentUserSession.getValueFactory().createValue(roleType.getNodeType())});
        }
        role.setProperty("j:roleGroup", roleType.getName());

        currentUserSession.save();
        this.setRoleBean(getRole(role.getIdentifier()));
        return true;
    }

    public boolean deleteRoles(String uuids) throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();
        String[] uuidsArray = uuids.split(",");
        for (String uuid : uuidsArray) {
            currentUserSession.getNodeByIdentifier(uuid).remove();
        }
        currentUserSession.save();
        return true;
    }

    private void addPermissionsForScope(RoleBean roleBean, String scope, Map<String, List<String>> permIdsMap, Map<String, List<String>> inheritedPermIdsMap) throws RepositoryException {
        final Map<String, Map<String, Map<String, PermissionBean>>> permissions = roleBean.getPermissions();
        if (!permissions.containsKey(scope)) {
            permissions.put(scope, new LinkedHashMap<String, Map<String, PermissionBean>>());
        }
        List<JCRNodeWrapper> allPermissions = getAllPermissions();

        String type = null;
        final Map<String, List<String>> globalPermissionsGroups = roleTypes.getPermissionsGroups();
        final Map<String, List<String>> permissionsGroupsForRoleType = roleBean.getRoleType().getPermissionsGroups();

        if (scope.equals("current")) {
            type = roleBean.getRoleType().getNodeType();
        } else {
            if (scope.equals("currentSite")) {
                type = "jnt:virtualsite";
            } else {
                try {
                    type = getSession().getNode(scope).getPrimaryNodeTypeName();
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        }
        if (type == null || (!globalPermissionsGroups.containsKey(type) && (permissionsGroupsForRoleType == null || !permissionsGroupsForRoleType.containsKey(type)))) {
            type = "nt:base";
        }
        if (permissionsGroupsForRoleType != null && permissionsGroupsForRoleType.containsKey(type)) {
            for (String s : permissionsGroupsForRoleType.get(type)) {
                permissions.get(scope).put(s, new TreeMap<String, PermissionBean>());
            }
        } else {
            for (String s : globalPermissionsGroups.get(type)) {
                permissions.get(scope).put(s, new TreeMap<String, PermissionBean>());
            }
        }

        Map<String, String> mappedPermissions = new HashMap<String, String>();

        Map<String, String> allGroups = new HashMap<String, String>();
        for (String s : permissions.get(scope).keySet()) {
            for (String s1 : Arrays.asList(s.split(","))) {
                allGroups.put(s1, s);
            }
        }


        if (usePermissionMapping) {
            for (Map.Entry<String, List<String>> entry : roleTypes.getPermissionsMapping().entrySet()) {
                String[] splitPath = entry.getKey().split("/");
                String permissionGroup = splitPath[2];
                if (allGroups.containsKey(permissionGroup)) {
                    Map<String, PermissionBean> p = permissions.get(scope).get(allGroups.get(permissionGroup));
                    PermissionBean bean = new PermissionBean();
                    bean.setUuid(null);
                    bean.setParentPath(StringUtils.substringBeforeLast(entry.getKey(), "/"));
                    bean.setName(StringUtils.substringAfterLast(entry.getKey(), "/"));
                    String localName = StringUtils.substringAfterLast(entry.getKey(), "/");
                    if (localName.contains(":")) {
                        localName = StringUtils.substringAfter(localName, ":");
                    }
                    String title = StringUtils.capitalize(localName.replaceAll("([A-Z])", " $0").replaceAll("[_-]", " ").toLowerCase());
                    final String rbName = localName.replaceAll("-", "_");
                    bean.setTitle(Messages.getInternal("label.permission." + rbName, LocaleContextHolder.getLocale(), title));
                    bean.setDescription(Messages.getInternal("label.permission." + rbName + ".description", LocaleContextHolder.getLocale(), ""));
                    bean.setPath(entry.getKey());
                    bean.setDepth(splitPath.length - 1);
                    bean.setMappedUuid(new ArrayList<String>());

                    p.put(entry.getKey(), bean);
                }
                for (String s : entry.getValue()) {
                    mappedPermissions.put(s, entry.getKey());
                }
            }
        }
        for (JCRNodeWrapper permissionNode : allPermissions) {
            JCRNodeWrapper permissionGroup = getPermissionGroupNode(permissionNode);
            if (allGroups.containsKey(permissionGroup.getName())) {
                Map<String, PermissionBean> p = permissions.get(scope).get(allGroups.get(permissionGroup.getName()));
                if (!permissionNode.hasProperty("j:requirePrivileged") || permissionNode.getProperty("j:requirePrivileged").getBoolean() == roleBean.getRoleType().isPrivileged()) {
                    final String permissionPath = getPermissionPath(permissionNode);
                    if (!mappedPermissions.containsKey(permissionPath) && mappedPermissions.containsKey(getPermissionPath(permissionNode.getParent()))) {
                        mappedPermissions.put(permissionPath, mappedPermissions.get(getPermissionPath(permissionNode.getParent())));
                    }
                    if (!p.containsKey(permissionPath) || permissionNode.getPath().startsWith("/permissions")) {
                        PermissionBean bean;
                        if (!mappedPermissions.containsKey(permissionPath)) {
                            bean = new PermissionBean();
                            setPermissionBeanProperties(permissionNode, bean);
                            p.put(permissionPath, bean);
                        } else {
                            bean = p.get(mappedPermissions.get(permissionPath));
                            bean.getMappedUuid().add(permissionNode.getIdentifier());
                        }
                        List<String> permIds = permIdsMap.get(scope);
                        List<String> inheritedPermIds = inheritedPermIdsMap.get(scope);
                        PermissionBean parentBean = p.get(bean.getParentPath());
                        if ((permIds != null && permIds.contains(permissionNode.getIdentifier()))
                                || (parentBean != null && parentBean.isSet())) {
                            bean.setSet(true);
                            while (parentBean != null && !parentBean.isSet() && !parentBean.isSuperSet()) {
                                parentBean.setPartialSet(true);
                                parentBean = p.get(parentBean.getParentPath());
                            }
                        }
                        parentBean = p.get(bean.getParentPath());
                        if ((inheritedPermIds != null && inheritedPermIds.contains(permissionNode.getIdentifier()))
                                || (parentBean != null && parentBean.isSuperSet())) {
                            bean.setSuperSet(true);
                            while (parentBean != null && !parentBean.isSet() && !parentBean.isSuperSet()) {
                                parentBean.setPartialSet(true);
                                parentBean = p.get(parentBean.getParentPath());
                            }
                        }
                    }
                }
            }
        }
    }

    private String getPermissionPath(JCRNodeWrapper permissionNode) {
        String path = permissionNode.getPath();
        if (path.startsWith("/modules")) {
            path = "/permissions/" + StringUtils.substringAfter(path, "/permissions/");
        }
        return path;
    }

    private int getPermissionDepth(JCRNodeWrapper permissionNode) throws RepositoryException {
        String path = permissionNode.getPath();
        if (path.startsWith("/modules")) {
            return permissionNode.getDepth() - 3;
        }
        return permissionNode.getDepth();
    }

    private JCRNodeWrapper getPermissionGroupNode(JCRNodeWrapper permissionNode) throws RepositoryException {
        JCRNodeWrapper permissionGroup = (JCRNodeWrapper) permissionNode.getAncestor(2);
        if (permissionGroup.isNodeType("jnt:module")) {
            permissionGroup = (JCRNodeWrapper) permissionNode.getAncestor(5);
        }
        return permissionGroup;
    }

    private void setPermissionBeanProperties(JCRNodeWrapper permissionNode, PermissionBean bean) throws RepositoryException {
        bean.setUuid(permissionNode.getIdentifier());

        bean.setParentPath(getPermissionPath(permissionNode.getParent()));
        bean.setName(permissionNode.getName());
        String localName = permissionNode.getName();
        if (localName.contains(":")) {
            localName = StringUtils.substringAfter(localName, ":");
        }
        String title = StringUtils.capitalize(localName.replaceAll("([A-Z])", " $0").replaceAll("[_-]", " ").toLowerCase());
        final String rbName = localName.replaceAll("-", "_");
        bean.setTitle(Messages.getInternal("label.permission." + rbName, LocaleContextHolder.getLocale(), title));
        bean.setDescription(Messages.getInternal("label.permission." + rbName + ".description", LocaleContextHolder.getLocale(), ""));
        bean.setPath(getPermissionPath(permissionNode));
        bean.setDepth(getPermissionDepth(permissionNode));
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String tab) {
        currentContext = tab;
        this.currentGroup = roleBean.getPermissions().get(currentContext).keySet().iterator().next();
    }

    public String getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
    }

    public void storeValues(String[] selectedValues, String[] partialSelectedValues) {
        Map<String, PermissionBean> permissionBeans = roleBean.getPermissions().get(currentContext).get(currentGroup);
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

        if (!roleBean.getPermissions().containsKey(newContext)) {
            addPermissionsForScope(roleBean, newContext, new HashMap<String, List<String>>(), new HashMap<String, List<String>>());
        }
        setCurrentContext(newContext);
    }

    public void removeContext(String scope) throws RepositoryException {

        if (roleBean.getPermissions().containsKey(scope)) {
            roleBean.getPermissions().remove(scope);
        }
        if (currentContext.equals(scope)) {
            setCurrentContext(roleBean.getPermissions().keySet().iterator().next());
        }
    }

    public void save() throws RepositoryException {
        JCRSessionWrapper currentUserSession = getSession();

        Map<String, List<Value>> permissions = new HashMap<String, List<Value>>();

        for (Map.Entry<String, Map<String, Map<String, PermissionBean>>> entry : roleBean.getPermissions().entrySet()) {
            ArrayList<Value> permissionValues = new ArrayList<Value>();
            permissions.put(entry.getKey(), permissionValues);
            for (Map<String, PermissionBean> map : entry.getValue().values()) {
                for (PermissionBean bean : map.values()) {
                    PermissionBean parentBean = map.get(bean.getParentPath());
                    if (bean.isSet() && (parentBean == null || !parentBean.isSet())) {
                        if (bean.getMappedUuid() != null) {
                            for (String s : bean.getMappedUuid()) {
                                permissionValues.add(currentUserSession.getValueFactory().createValue(s, PropertyType.WEAKREFERENCE));
                            }
                        } else {
                            permissionValues.add(currentUserSession.getValueFactory().createValue(bean.getUuid(), PropertyType.WEAKREFERENCE));
                        }
                    }
                }
            }
        }

        JCRNodeWrapper role = currentUserSession.getNodeByIdentifier(roleBean.getUuid());
        Set<String> externalPermissionNodes = new HashSet<String>();
        for (Map.Entry<String, List<Value>> s : permissions.entrySet()) {
            String key = s.getKey();
            if (key.equals("current")) {
                role.setProperty("j:permissions", permissions.get("current").toArray(new Value[permissions.get("current").size()]));
            } else {
                if (key.equals("/")) {
                    key = "root-access";
                } else {
                    key = ISO9075.encode((key.startsWith("/") ? key.substring(1) : key).replace("/", "-")) + "-access";
                }
                if (!s.getValue().isEmpty()) {
                    if (!role.hasNode(key)) {
                        JCRNodeWrapper extPermissions = role.addNode(key, "jnt:externalPermissions");
                        extPermissions.setProperty("j:path", s.getKey());
                        extPermissions.setProperty("j:permissions", s.getValue().toArray(new Value[s.getValue().size()]));
                    } else {
                        role.getNode(key).setProperty("j:permissions", s.getValue().toArray(new Value[s.getValue().size()]));
                    }
                    externalPermissionNodes.add(key);
                }
            }
        }
        NodeIterator ni = role.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.getPrimaryNodeTypeName().equals("jnt:externalPermissions") && !externalPermissionNodes.contains(next.getName())) {
                next.remove();
            }
        }
        role.setProperty("jcr:title", roleBean.getTitle());
        role.setProperty("jcr:description", roleBean.getDescription());
        role.setProperty("j:hidden", roleBean.isHidden());
        currentUserSession.save();
    }

    public List<JCRNodeWrapper> getAllPermissions() throws RepositoryException {
        if (allPermissions != null) {
            return allPermissions;
        }

        allPermissions = new ArrayList<JCRNodeWrapper>();
        JCRSessionWrapper currentUserSession = getSession();

        QueryManager qm = currentUserSession.getWorkspace().getQueryManager();
        String statement = "select * from [jnt:permission]";

        Query q = qm.createQuery(statement, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            int depth = 2;
            if (((JCRNodeWrapper) next.getAncestor(1)).isNodeType("jnt:modules")) {
                depth = 5;
            }
            if (next.getDepth() >= depth) {
                allPermissions.add(next);
            }
        }
        Collections.sort(allPermissions, new Comparator<JCRNodeWrapper>() {
            @Override
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                if (getPermissionPath(o1).equals(getPermissionPath(o2))) {
                    return -o1.getPath().compareTo(o2.getPath());
                }
                return getPermissionPath(o1).compareTo(getPermissionPath(o2));
            }
        });

        return allPermissions;
    }

    public void storeDetails(String title, String description, Boolean hidden) {
        roleBean.setTitle(title);
        roleBean.setDescription(description);
        roleBean.setHidden(hidden != null && hidden);
    }

    private String getMessage(String key) {
        return Messages.get("resources.JahiaRolesManager", key, LocaleContextHolder.getLocale());
    }

}
