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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Sep 11, 2008 - 4:08:23 PM
 */
public class GWTJahiaNodeACL implements Serializable {
    private List<GWTJahiaNodeACE> ace;
    private Map<String,List<String>> availableRoles;
    private Map<String,String> rolesLabels;
    private Map<String,String> rolesTooltips;
    private Map<String,List<String>> roleDependencies;
    private boolean breakAllInheritance = false;
    public GWTJahiaNodeACL() {
    }

    public GWTJahiaNodeACL(List<GWTJahiaNodeACE> ace) {
        this.ace = ace;
    }

    public List<GWTJahiaNodeACE> getAce() {
        return ace;
    }

    public void setAce(List<GWTJahiaNodeACE> ace) {
        this.ace = ace;
    }

    public Map<String,List<String>> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(Map<String, List<String>> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public Map<String, String> getRolesLabels() {
        return rolesLabels;
    }

    public void setRolesLabels(Map<String, String> rolesLabels) {
        this.rolesLabels = rolesLabels;
    }

    public void setRolesTooltips(Map<String, String> rolesTooltips) {
        this.rolesTooltips = rolesTooltips;
    }

    public Map<String,List<String>> getRoleDependencies() {
        return roleDependencies;
    }

    public void setRoleDependencies(Map<String, List<String>> roleDependencies) {
        this.roleDependencies = roleDependencies;
    }

    public boolean isInheritanceBroken() {
        boolean inheritanceBroken = breakAllInheritance;
        if (!inheritanceBroken) {
            for (GWTJahiaNodeACE ace : getAce()) {
                inheritanceBroken = !ace.isInherited();
                if (inheritanceBroken) {
                    break;
                }
            }
        }
        return inheritanceBroken;
    }    
    
    public boolean isBreakAllInheritance() {
        return breakAllInheritance;
    }

    public void setBreakAllInheritance(boolean breakAllInheritance) {
        this.breakAllInheritance = breakAllInheritance;
    }

    public GWTJahiaNodeACL cloneObject() {
        GWTJahiaNodeACL clone = new GWTJahiaNodeACL();
        List<GWTJahiaNodeACE> aceClone = new ArrayList<GWTJahiaNodeACE>();
        clone.setAvailableRoles(getAvailableRoles());
        clone.setRolesLabels(getRolesLabels());
        clone.setRolesTooltips(getRolesTooltips());
        clone.setRoleDependencies(getRoleDependencies());
        for (GWTJahiaNodeACE nodeACE : ace) {
            aceClone.add(nodeACE.cloneObject());
        }
        clone.setAce(aceClone);
        clone.setBreakAllInheritance(isBreakAllInheritance());
        return clone;
    }

    public Map<String, String> getRolesTooltips() {
        return rolesTooltips;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaNodeACL that = (GWTJahiaNodeACL) o;

        if (breakAllInheritance != that.breakAllInheritance) return false;
        if (ace != null ? !ace.equals(that.ace) : that.ace != null) return false;
        if (availableRoles != null ? !availableRoles.equals(that.availableRoles) : that.availableRoles != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ace != null ? ace.hashCode() : 0;
        result = 31 * result + (availableRoles != null ? availableRoles.hashCode() : 0);
        result = 31 * result + (breakAllInheritance ? 1 : 0);
        return result;
    }
}
