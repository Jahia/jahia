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
        return declaredAggregates.toArray(new Privilege[declaredAggregates.size()]);
    }

    public Privilege[] getAggregatePrivileges() {
        return aggregates.toArray(new Privilege[aggregates.size()]);
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
