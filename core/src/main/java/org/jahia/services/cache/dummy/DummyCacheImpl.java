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
package org.jahia.services.cache.dummy;

import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;

import java.util.Set;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 27 août 2008
 * Time: 14:10:29
 * To change this template use File | Settings | File Templates.
 */
public class DummyCacheImpl implements CacheImplementation {

    private String name;

    public DummyCacheImpl(String name) {
        setName(name);
    }

    public boolean containsKey(Object key) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object get(Object key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void put(Object key, String[] groups, Object value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEmpty() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int size() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Object> getKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getGroupsSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getGroupsKeysTotal() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void flushAll(boolean propagate) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void flushGroup(String groupName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove(Object key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addListener(CacheListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeListener(CacheListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getCacheLimit() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCacheLimit(long limit) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getCacheGroupsLimit() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCacheGroupsLimit(long groupsLimit) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set getGroupKeys(String groupName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
