package org.apache.jackrabbit.core.security;

import javax.jcr.security.Privilege;
import java.util.HashSet;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: 1/6/11
* Time: 13:39
* To change this template use File | Settings | File Templates.
*/
public class PrivilegeImpl implements Privilege {
    private String prefixedName;
    private String expandedName;
    private boolean isAbstract;
    private Set<Privilege> declaredAggregates;
    private Set<Privilege> aggregates;

    PrivilegeImpl(String prefixedName, String expandedName, boolean anAbstract, Set<Privilege> declaredAggregates) {
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
        return expandedName != null ? expandedName.hashCode() : 0;
    }
}
