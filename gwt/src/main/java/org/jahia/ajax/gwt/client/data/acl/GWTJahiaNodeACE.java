/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
    private Map<String, Boolean> roles;
    private Map<String, Boolean> inheritedRoles;
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

    public Map<String, Boolean> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, Boolean> roles) {
        this.roles = roles;
    }

    public Map<String, Boolean> getInheritedRoles() {
        return inheritedRoles;
    }

    public void setInheritedRoles(Map<String, Boolean> inheritedRoles) {
        this.inheritedRoles = inheritedRoles;
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
        for (String s : roles.keySet()) {
            permsClone.put(s, roles.get(s));
        }
        clone.setRoles(permsClone);

        permsClone = new HashMap<String, Boolean>();
        for (String s : inheritedRoles.keySet()) {
            permsClone.put(s, inheritedRoles.get(s));
        }
        clone.setInheritedRoles(permsClone);

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
        if (inheritedRoles != null ? !inheritedRoles.equals(that.inheritedRoles) : that.inheritedRoles != null)
            return false;
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) return false;
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
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (inheritedRoles != null ? inheritedRoles.hashCode() : 0);
        result = 31 * result + (hidden ? 1 : 0);
        return result;
    }
}
