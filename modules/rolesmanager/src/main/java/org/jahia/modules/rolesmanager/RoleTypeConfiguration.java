/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
 */

package org.jahia.modules.rolesmanager;

import java.util.*;

public class RoleTypeConfiguration {
    private LinkedHashMap<String, RoleType> roleTypes = new LinkedHashMap<String, RoleType>();

    private Map<String,List<String>> permissionsGroups;

    private Map<String,List<String>> permissionsMapping;

    public RoleTypeConfiguration() {
    }

    public void setRoleTypes(List<RoleType> roleTypesSet) {
        for (RoleType roleType : roleTypesSet) {
            roleTypes.put(roleType.getName(), roleType);
        }
    }

    public RoleType get(String name) {
        return roleTypes.get(name);
    }

    public Collection<RoleType> getValues() {
        return roleTypes.values();
    }


    public Map<String, List<String>> getPermissionsGroups() {
        return permissionsGroups;
    }

    public void setPermissionsGroups(Map<String, List<String>> permissionsGroups) {
        this.permissionsGroups = permissionsGroups;
    }

    public Map<String, List<String>> getPermissionsMapping() {
        return permissionsMapping;
    }

    public void setPermissionsMapping(Map<String, List<String>> permissionsMapping) {
        this.permissionsMapping = permissionsMapping;
    }
}
