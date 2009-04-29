/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.acl;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 11, 2008
 * Time: 4:11:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaNodeACE implements Serializable {

    private char principalType;
    private String principal;
    private String principalKey;
    private boolean isInherited;
    private String inheritedFrom;
    private Map<String, String> permissions;
    private Map<String, String> inheritedPermissions;

    public GWTJahiaNodeACE() {
    }

    public char getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(char principalType) {
        this.principalType = principalType;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPrincipalKey() {
        return principalKey;
    }

    public void setPrincipalKey(String principalkey) {
        this.principalKey = principalkey;
    }

    public boolean isInherited() {
        return isInherited;
    }

    public void setInherited(boolean inherited) {
        isInherited = inherited;
    }

    public String getInheritedFrom() {
        return inheritedFrom;
    }

    public void setInheritedFrom(String inheritedFrom) {
        this.inheritedFrom = inheritedFrom;
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getInheritedPermissions() {
        return inheritedPermissions;
    }

    public void setInheritedPermissions(Map<String, String> inheritedPermissions) {
        this.inheritedPermissions = inheritedPermissions;
    }

    public GWTJahiaNodeACE cloneObject() {
        GWTJahiaNodeACE clone = new GWTJahiaNodeACE();
        clone.setPrincipal(principal);
        clone.setPrincipalKey(principalKey);
        clone.setPrincipalType(principalType);
        clone.setInherited(isInherited);
        clone.setInheritedFrom(inheritedFrom);

        Map<String, String> permsClone = new HashMap<String, String>();
        for (String s : permissions.keySet()) {
            permsClone.put(s, permissions.get(s));
        }
        clone.setPermissions(permsClone);

        permsClone = new HashMap<String, String>();
        for (String s : inheritedPermissions.keySet()) {
            permsClone.put(s, inheritedPermissions.get(s));
        }
        clone.setInheritedPermissions(permsClone);
        return clone;
    }

}
