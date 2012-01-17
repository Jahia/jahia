/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data.acl;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 * User: toto
 * Date: Sep 11, 2008
 * Time: 4:11:31 PM
 * 
 */
public class GWTJahiaNodeACE implements Serializable {

    private char principalType;
    private String principal;
    private String principalKey;
    private boolean isInherited;
    private String inheritedFrom;
    private Map<String, Boolean> permissions;
    private Map<String, Boolean> inheritedPermissions;
    private boolean hidden = false;
    private String principalDisplayName;

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
        this.principalDisplayName = principal;
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

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Boolean> getInheritedPermissions() {
        return inheritedPermissions;
    }

    public void setInheritedPermissions(Map<String, Boolean> inheritedPermissions) {
        this.inheritedPermissions = inheritedPermissions;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getPrincipalDisplayName() {
        return principalDisplayName;
    }

    public void setPrincipalDisplayName(String principalDisplayName) {
        this.principalDisplayName = principalDisplayName;
    }

    public GWTJahiaNodeACE cloneObject() {
        GWTJahiaNodeACE clone = new GWTJahiaNodeACE();
        clone.setPrincipal(principal);
        clone.setPrincipalDisplayName(principalDisplayName);
        clone.setPrincipalKey(principalKey);
        clone.setPrincipalType(principalType);
        clone.setInherited(isInherited);
        clone.setInheritedFrom(inheritedFrom);

        Map<String, Boolean> permsClone = new HashMap<String, Boolean>();
        for (String s : permissions.keySet()) {
            permsClone.put(s, permissions.get(s));
        }
        clone.setPermissions(permsClone);

        permsClone = new HashMap<String, Boolean>();
        for (String s : inheritedPermissions.keySet()) {
            permsClone.put(s, inheritedPermissions.get(s));
        }
        clone.setInheritedPermissions(permsClone);

        clone.setHidden(hidden);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaNodeACE that = (GWTJahiaNodeACE) o;

        if (hidden != that.hidden) return false;
        if (isInherited != that.isInherited) return false;
        if (principalType != that.principalType) return false;
        if (inheritedFrom != null ? !inheritedFrom.equals(that.inheritedFrom) : that.inheritedFrom != null)
            return false;
        if (inheritedPermissions != null ? !inheritedPermissions.equals(that.inheritedPermissions) : that.inheritedPermissions != null)
            return false;
        if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null) return false;
        if (principal != null ? !principal.equals(that.principal) : that.principal != null) return false;
        if (principalKey != null ? !principalKey.equals(that.principalKey) : that.principalKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) principalType;
        result = 31 * result + (principal != null ? principal.hashCode() : 0);
        result = 31 * result + (principalKey != null ? principalKey.hashCode() : 0);
        result = 31 * result + (isInherited ? 1 : 0);
        result = 31 * result + (inheritedFrom != null ? inheritedFrom.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        result = 31 * result + (inheritedPermissions != null ? inheritedPermissions.hashCode() : 0);
        result = 31 * result + (hidden ? 1 : 0);
        return result;
    }
}
