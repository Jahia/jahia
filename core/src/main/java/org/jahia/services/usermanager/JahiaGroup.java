package org.jahia.services.usermanager;

import java.security.Principal;
import java.security.acl.Group;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kevan
 * This class is keep to maintain compatibility with olds users/groups providers
 * @deprecated use the org.jahia.services.usermanager.Group class instead
 */
@Deprecated
public abstract class JahiaGroup implements JahiaPrincipal, Group {

    private static final long serialVersionUID = 3192050315335252786L;

    protected String mGroupname;
    protected String mGroupKey;
    protected boolean hidden = false;
    protected int mSiteID;
    protected boolean preloadedGroups;
    protected Map<String, Boolean> membership = new ConcurrentHashMap<String, Boolean>();
    protected Set<Principal> mMembers;

    @Deprecated
    public abstract Properties getProperties ();

    @Deprecated
    public abstract String getProperty (String key);

    @Deprecated
    public abstract boolean removeProperty (String key);

    @Deprecated
    public abstract boolean setProperty (String key, String value);

    public abstract void addMembers(final Collection<Principal> principals);

    public String getGroupKey () {
        return mGroupKey;
    }

    public String getGroupname () {
        return mGroupname;
    }

    @Override
    public Enumeration<? extends Principal> members() {
        return null;
    }

    @Override
    public String getLocalPath() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Deprecated
    public int getSiteID () {
        return mSiteID;
    }

    @Deprecated
    public Collection<Principal> getMembers() {
        return getMembersMap();
    }

    @Deprecated
    abstract protected Set<Principal> getMembersMap();

    @Deprecated
    public Set<Principal> getRecursiveUserMembers () {
        return null;
    }

    @Deprecated
    public boolean removeMembers () {
        for (Principal aMember : getMembersMap()) {
            removeMember (aMember);
        }
        return true;
    }

    @Deprecated
    public boolean isPreloadedGroups() {
        return preloadedGroups;
    }

    @Deprecated
    public boolean isHidden() {
        return hidden;
    }

    @Deprecated
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    abstract public int hashCode();

    @Override
    abstract public String toString();
}