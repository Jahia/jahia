package org.jahia.services.render.filter.cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to associate a UUID and a set for special purposes in the cache
 */
public class UUIDSet implements Serializable{
    private String uuid;
    private Set<String> set;
    public UUIDSet(String uuid) {
        this.uuid = uuid;
        this.set = new HashSet<String>();
    }

    public String getUuid() {
        return uuid;
    }

    public Set<String> getSet() {
        return set;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UUIDSet uuidSet = (UUIDSet) o;

        return uuid.equals(uuidSet.uuid);

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
