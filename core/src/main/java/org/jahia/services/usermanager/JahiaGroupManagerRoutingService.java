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

 package org.jahia.services.usermanager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.sites.JahiaSite;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class JahiaGroupManagerRoutingService extends JahiaGroupManagerService {
// ------------------------------ FIELDS ------------------------------

    static private JahiaGroupManagerRoutingService mInstance = null;

    private Map<String, JahiaGroupManagerProvider> providersTable = null;
    private SortedSet<JahiaGroupManagerProvider> sortedProviders = null;
    private JahiaGroupManagerProvider defaultProviderInstance = null;


// -------------------------- STATIC METHODS --------------------------

    /**
     * Create an new instance of the Group Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the Group Manager Service.
     */
    public static JahiaGroupManagerRoutingService getInstance () {
        if (mInstance == null) {
       		mInstance = new JahiaGroupManagerRoutingService ();
       	}
        return mInstance;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    protected JahiaGroupManagerRoutingService () {
        providersTable = new HashMap<String, JahiaGroupManagerProvider>();

        sortedProviders = new TreeSet<JahiaGroupManagerProvider> (new Comparator<JahiaGroupManagerProvider> () {
            public int compare (JahiaGroupManagerProvider o1, JahiaGroupManagerProvider o2) {
                return (o1.getPriority () - o2.getPriority ());
            }
        });
    }

// -------------------------- OTHER METHODS --------------------------


    public void start() throws JahiaInitializationException {
    	// do nothing
    }

    public void stop() throws JahiaException {
    	// do nothing
    }

    public JahiaGroup createGroup(final int siteID, final String name, final Properties properties, final boolean hidden) {
        return (JahiaGroup) routeCallOne(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.createGroup(siteID, name, properties, hidden);
            }
        }, null, properties);
    }

    public boolean deleteGroup (final JahiaGroup group) {
        if (group == null) {
            return false;
        }

        Boolean resultBool = (Boolean) routeCallOne(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                if (p.deleteGroup(group)) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }
        }, null, group.getProperties());

        return resultBool != null && resultBool.booleanValue();
    }

//------------------------------------------------------------------------


    public List<JahiaSite> getAdminGrantedSites (final JahiaUser user)
            throws org.jahia.exceptions.JahiaException {
        List<List<JahiaSite>> resultList = (List<List<JahiaSite>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getAdminGrantedSites(user);
            }
        }, null);
        List<JahiaSite> sitesList = new ArrayList<JahiaSite>();
        Iterator<List<JahiaSite>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<JahiaSite> curResult = resultEnum.next ();
            if (curResult != null) {
                for (int i = 0; i < curResult.size (); i++) {
                    if (!sitesList.contains (curResult.get (i))) {
                        sitesList.add (curResult.get (i));
                    }
                }
            }
        }
        return sitesList;
    }

    public JahiaGroup getAdministratorGroup (final int siteID) {
        return (JahiaGroup) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getAdministratorGroup(siteID);
            }
        }, null);
    }

    public List<String> getGroupList () {
        List<String> groupList = new ArrayList<String>();

        List<List<String>> resultList = (List<List<String>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getGroupList();
            }
        }, null);

        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<String> curResult = resultEnum.next ();
            groupList.addAll (curResult);
        }
        return groupList;
    }

    public List<String> getGroupList (final int siteID) {
        List<String> groupList = new ArrayList<String>();

        List<List<String>> resultList = (List<List<String>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getGroupList(siteID);
            }
        }, null);
        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<String> curResult = resultEnum.next ();
            if (curResult != null) {
                groupList.addAll (curResult);
            }
        }
        return groupList;
    }

    public List<String> getGroupnameList () {
        List<String> groupNameList = new ArrayList<String>();

        List<List<String>> resultList = (List<List<String>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getGroupnameList();
            }
        }, null);
        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<String> curResult = resultEnum.next ();
            groupNameList.addAll (curResult);
        }
        return groupNameList;
    }

    public List<String> getGroupnameList (final int siteID) {
        List<List<String>> resultList = (List<List<String>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getGroupnameList(siteID);
            }
        }, null);

        List<String> groupNameList = new ArrayList<String>();

        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            Collection<String> curResult = resultEnum.next ();
            groupNameList.addAll (curResult);
        }
        return groupNameList;
    }

    public JahiaGroup getGuestGroup (final int siteID) {
        return (JahiaGroup) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getGuestGroup(siteID);
            }
        }, null);
    }

    /**
     * Returns a List of JahiaGroupManagerProvider object describing the
     * available group management providers
     *
     * @return result a List of JahiaGroupManagerProvider objects that describe
     *         the providers. This will never be null but may be empty if no providers
     *         are available.
     */
    public List<? extends JahiaGroupManagerProvider> getProviderList () {
        return new ArrayList<JahiaGroupManagerProvider>(sortedProviders);
    }

    /**
     * Returns a List of provider instances to call depending on the specified
     * list of provider names.
     *
     * @param providersToCall a List containing String that contain names
     *                        of the providers to call. If no list is specified, the result list is
     *                        composed of the internal list of providers
     *
     * @return a List of provider instances, may be empty if none was found,
     *         but never null. If no name list is specified, returns the internal list
     *         of providers
     */
    private List<? extends JahiaGroupManagerProvider> getProvidersToCall (List<? extends JahiaGroupManagerProvider> providersToCall) {
        // we use this temporary List in order to avoid having twice
        // the same code for the dispatching...
        List<JahiaGroupManagerProvider> tmpProviderInstances = new ArrayList<JahiaGroupManagerProvider>();

        if (providersToCall != null) {
            if (providersToCall.size () >= 1) {
                Iterator<? extends JahiaGroupManagerProvider> providerEnum = providersToCall.iterator();
                while (providerEnum.hasNext ()) {
                    Object curProviderEntry = providerEnum.next ();
                    if (curProviderEntry instanceof String) {
                        String curProviderKey = (String) curProviderEntry;
                        JahiaGroupManagerProvider curProviderInstance = (JahiaGroupManagerProvider) providersTable.get (curProviderKey);
                        if (curProviderInstance != null) {
                            tmpProviderInstances.add (curProviderInstance);
                        }
                    }
                }
            }
        }

        if (tmpProviderInstances.size () == 0) {
            Iterator<? extends JahiaGroupManagerProvider> providerIter = sortedProviders.iterator ();
            while (providerIter.hasNext ()) {
                JahiaGroupManagerProvider curProvider = (JahiaGroupManagerProvider) providerIter.next ();
                tmpProviderInstances.add (curProvider);
            }
        }

        return tmpProviderInstances;
    }

    /*public TreeSet getServerList (String name) {
        return (TreeSet)serversTable.get(name);
    }*/

    public List<String> getUserMembership (final JahiaUser user) {
        List<List<String>> resultList = (List<List<String>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getUserMembership(user);
            }
        }, null);
        List<String> userMembership = new ArrayList<String>();
        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<String> curResult = resultEnum.next ();
            if (curResult != null) {
                userMembership.addAll (curResult);
            }
        }
        return userMembership;
    }

    public JahiaGroup getUsersGroup (final int siteID) {
        return (JahiaGroup) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.getUsersGroup(siteID);
            }
        }, null);
    }

    public boolean groupExists (final int siteID, final String name) {
        Boolean resultBool = (Boolean) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                if (p.groupExists(siteID, name)) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }
        }, null);

        return resultBool != null && resultBool.booleanValue();
    }

    public JahiaGroup lookupGroup (final String groupKey) {
        return (JahiaGroup) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.lookupGroup(groupKey);
            }
        }, null);
    }

    public JahiaGroup lookupGroup (final int siteID, final String name) {
        return (JahiaGroup) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.lookupGroup(siteID, name);
            }
        }, null);
    }

    public boolean removeUserFromAllGroups (final JahiaUser user) {
        List<Boolean> resultList = (List<Boolean>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return Boolean.valueOf(p.removeUserFromAllGroups(user));
            }
        }, null);

        boolean success = true;
        Iterator<Boolean> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            Boolean resultBool = (Boolean) resultEnum.next ();
            if (!resultBool.booleanValue ()) {
                success = false;
                break;
            }
        }
        return success;
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*")
     *
     * @return Set a set of JahiaGroup elements that correspond to those
     *         search criterias
     */
    public Set<JahiaGroup> searchGroups (final int siteID, final Properties searchCriterias) {
        Set<JahiaGroup> groupList = new HashSet<JahiaGroup>();

        List<Set<JahiaGroup>> resultList = (List<Set<JahiaGroup>>)routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.searchGroups(siteID, searchCriterias);
            }
        }, null);
        
        Iterator<Set<JahiaGroup>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            Set<JahiaGroup> curResult = resultEnum.next ();
            groupList.addAll (curResult);
        }
        return groupList;
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param providerKey     key of the provider in which to search, may be
     *                        obtained by calling getProviderList()
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaGroup elements that correspond to those
     *         search criterias
     */
    public Set<JahiaGroup> searchGroups (final String providerKey, final int siteID, final Properties searchCriterias) {
        List<String> providerList = new ArrayList<String>();
        providerList.add (providerKey);

        return (Set<JahiaGroup>) routeCallOne(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                return p.searchGroups(siteID, searchCriterias);
            }
        }, providerList, null);
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public void updateCache(final JahiaGroup jahiaGroup) {
        routeCallOne(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                p.updateCache(jahiaGroup);
                return null;
            }
        }, null, jahiaGroup.getProperties());
    }

    private Object routeCallOne(Command v, List<String> providersToCall, Properties userProperties) {
        // we're calling only one of the provider, we must determine
        // which one by using the user properties matched against the
        // criteria , or by using the providersToCall parameter
        JahiaGroupManagerProvider providerInstance = null;
        if (providersToCall != null) {
            if (providersToCall.size () >= 1) {
                Object providerItem = providersToCall.get(0);
                if (providerItem instanceof String) {
                    String providerKey = (String) providerItem;
                    providerInstance = (JahiaGroupManagerProvider) providersTable.get (
                            providerKey);
                }
            }
        }
        if (providerInstance == null) {
            // fallback, we must find at least one provider to call.
            providerInstance = defaultProviderInstance;
            if (providerInstance == null) {
                // what no default provider ?? exit immediately.
                return null;
            }
        }

        return v.execute(providerInstance);
    }

    private Object routeCallAllUntilSuccess(Command v, List<? extends JahiaGroupManagerProvider> providersToCall) {
        // we're calling the providers in order, until one returns
        // a success condition (to be defined)

        Iterator<? extends JahiaGroupManagerProvider> providerEnum = getProvidersToCall (providersToCall).iterator();
        Object result = null;
        while (providerEnum.hasNext ()) {
            JahiaGroupManagerProvider curProvider =
                    providerEnum.next ();
            result = v.execute(curProvider);
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    private List<?> routeCallAll(Command v, List<? extends JahiaGroupManagerProvider> providersToCall) {
        // we're calling all the providers
        List<Object> results = new ArrayList<Object>();

        Iterator<? extends JahiaGroupManagerProvider> providerEnum = getProvidersToCall (providersToCall).iterator();

        while (providerEnum.hasNext ()) {
            JahiaGroupManagerProvider curProvider =
                    providerEnum.next ();
            results.add(v.execute(curProvider));
        }
        return results;
    }

	public boolean isGroupNameSyntaxCorrect(final String name) {
		Boolean result = (Boolean) routeCallOne(new Command() {
			public Object execute(JahiaGroupManagerProvider p) {
				return p.isGroupNameSyntaxCorrect(name) ? Boolean.TRUE
				        : Boolean.FALSE;
			}
		}, null, null);
		return result.booleanValue();
	}

// -------------------------- INNER CLASSES --------------------------

    interface Command {
        Object execute(JahiaGroupManagerProvider p);
    }

	@Override
    public void registerProvider(JahiaGroupManagerProvider provider) {
	    providersTable.put(provider.getKey(), provider);
	    sortedProviders.add(provider);
	    if (defaultProviderInstance == null || provider.isDefaultProvider()) {
	    	defaultProviderInstance = provider;
	    }
    }

    @Override
    public void flushCache() {
        routeCallAll(new Command() {
            public Object execute(JahiaGroupManagerProvider p) {
                p.flushCache();
                return null;
            }
        }, getProviderList());
    }

    public void setDefaultProvider(JahiaGroupManagerProvider defaultProvider) {
		defaultProvider.setDefaultProvider(true);
		defaultProvider.setGroupManagerService(this);
		registerProvider(defaultProvider);
    }
    
    @Override
    public JahiaGroupManagerProvider getProvider(String name) {
        return providersTable.get(name);
    }
}
