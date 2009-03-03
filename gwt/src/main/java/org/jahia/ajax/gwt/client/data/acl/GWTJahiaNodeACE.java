/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
