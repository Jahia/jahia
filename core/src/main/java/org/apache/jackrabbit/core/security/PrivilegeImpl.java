/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.security;

import javax.jcr.security.Privilege;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
* User: toto
* Date: 1/6/11
* Time: 13:39
*/
public class PrivilegeImpl implements Privilege, Serializable {
    private String prefixedName;
    private String expandedName;
    private boolean isAbstract;
    private Set<Privilege> declaredAggregates;
    private Set<Privilege> aggregates;
    private transient int hash;
    private String nodePath;
    private Privilege[] aggregatePrivileges = null;
    private Privilege[] declaredPrivileges = null;

    PrivilegeImpl(String prefixedName, String expandedName, boolean anAbstract, Set<Privilege> declaredAggregates, String nodePath) {
        this.prefixedName = prefixedName;
        this.expandedName = expandedName;
        isAbstract = anAbstract;
        this.declaredAggregates = declaredAggregates;
        this.aggregates = new HashSet<Privilege>(declaredAggregates);
        for (Privilege priv : declaredAggregates) {
            for (Privilege privilege : priv.getAggregatePrivileges()) {
                aggregates.add(privilege);
            }
        }
        this.nodePath = nodePath;
    }

    void addPrivileges(Set<Privilege> p) {
        declaredAggregates.removeAll(p);
        if (declaredAggregates.addAll(p)) {
            aggregates.addAll(declaredAggregates);
            for (Privilege priv : p) {
                for (Privilege privilege : priv.getAggregatePrivileges()) {
                    aggregates.add(privilege);
                }
            }
        }
        declaredPrivileges=null;
        aggregatePrivileges=null;
    }

    public String getName() {
        return expandedName;
    }

    public String getPrefixedName() {
        return prefixedName;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isAggregate() {
        return !declaredAggregates.isEmpty();
    }

    public Privilege[] getDeclaredAggregatePrivileges() {
        if(declaredPrivileges==null) {
            declaredPrivileges = declaredAggregates.toArray(new Privilege[declaredAggregates.size()]);
        }
        return declaredPrivileges;
    }

    public Privilege[] getAggregatePrivileges() {
        if(aggregatePrivileges==null) {
            aggregatePrivileges = aggregates.toArray(new Privilege[aggregates.size()]);
        }
        return aggregatePrivileges;
    }

    public String getNodePath() {
        return nodePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrivilegeImpl that = (PrivilegeImpl) o;

        if (expandedName != null ? !expandedName.equals(that.expandedName) : that.expandedName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        // Name is immutable, we can store the computed hash code value
        int h = hash;
        if (h == 0 && expandedName != null) {
            hash = expandedName.hashCode();
        }
        return h;
    }
}
