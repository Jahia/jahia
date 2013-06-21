/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
import java.util.Collections;
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

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.utils.ClassLoaderUtils;
import org.jahia.utils.ClassLoaderUtils.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import javax.jcr.RepositoryException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class JahiaGroupManagerRoutingService extends JahiaGroupManagerService implements ApplicationEventPublisherAware {
    
    private static final Logger logger = LoggerFactory.getLogger(JahiaGroupManagerRoutingService.class);

    static private JahiaGroupManagerRoutingService mInstance = null;

    private Map<String, JahiaGroupManagerProvider> providersTable = null;
    private SortedSet<JahiaGroupManagerProvider> sortedProviders = null;
    private JahiaGroupManagerProvider defaultProviderInstance = null;
    private List<String> jahiaJcrEnforcedGroups;
    private String jahiaJcrEnforcedGroupsProviderKey;

    private ApplicationEventPublisher applicationEventPublisher;


    /**
     * Create an new instance of the Group Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the Group Manager Service.
     */
    public static JahiaGroupManagerRoutingService getInstance () {
        if (mInstance == null) {
            mInstance = new JahiaGroupManagerRoutingService();
        }
        return mInstance;
    }

    protected JahiaGroupManagerRoutingService () {
        providersTable = new HashMap<String, JahiaGroupManagerProvider>();

        sortedProviders = new TreeSet<JahiaGroupManagerProvider> (new Comparator<JahiaGroupManagerProvider> () {
            public int compare (JahiaGroupManagerProvider o1, JahiaGroupManagerProvider o2) {
                return (o1.getPriority () - o2.getPriority ());
            }
        });
    }

    public void start() throws JahiaInitializationException {
    	// do nothing
    }

    public void stop() throws JahiaException {
    	// do nothing
    }

    public JahiaGroup createGroup(final int siteID, final String name, final Properties properties, final boolean hidden) {
        return defaultProviderInstance != null ? defaultProviderInstance.createGroup(siteID, name, properties, hidden) : null;
    }

    public boolean deleteGroup(final JahiaGroup group) {
        return group != null && (defaultProviderInstance != null ? defaultProviderInstance.deleteGroup(group) : false);
    }

    public List<JahiaSite> getAdminGrantedSites(final JahiaUser user) throws JahiaException {
        if (isSingleProvider()) {
            return defaultProviderInstance.getAdminGrantedSites(user);
        }
        List<List<JahiaSite>> resultList = routeCallAll(new Command<List<JahiaSite>>() {
            public List<JahiaSite> execute(JahiaGroupManagerProvider p) {
                return p.getAdminGrantedSites(user);
            }
        });
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

    private boolean isSingleProvider() {
        return providersTable.size() == 1;
    }

    public JahiaGroup getAdministratorGroup (final int siteID) {
        if (isSingleProvider()) {
            return defaultProviderInstance.getAdministratorGroup(siteID);
        }
        return routeCallAllUntilSuccess(new Command<JahiaGroup>() {
            public JahiaGroup execute(JahiaGroupManagerProvider p) {
                return p.getAdministratorGroup(siteID);
            }
        });
    }

    public List<String> getGroupList () {
        if (isSingleProvider()) {
            return defaultProviderInstance.getGroupList();
        }
        List<String> groupList = new ArrayList<String>();

        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaGroupManagerProvider p) {
                return p.getGroupList();
            }
        });

        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<String> curResult = resultEnum.next ();
            groupList.addAll (curResult);
        }
        return groupList;
    }

    public List<String> getGroupList (final int siteID) {
        if (isSingleProvider()) {
            return defaultProviderInstance.getGroupList(siteID);
        }
        List<String> groupList = new ArrayList<String>();

        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaGroupManagerProvider p) {
                return p.getGroupList(siteID);
            }
        });
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
        if (isSingleProvider()) {
            return defaultProviderInstance.getGroupnameList();
        }
        List<String> groupNameList = new ArrayList<String>();

        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaGroupManagerProvider p) {
                return p.getGroupnameList();
            }
        });
        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List<String> curResult = resultEnum.next ();
            groupNameList.addAll (curResult);
        }
        return groupNameList;
    }

    public List<String> getGroupnameList (final int siteID) {
        if (isSingleProvider()) {
            return defaultProviderInstance.getGroupnameList(siteID);
        }
        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaGroupManagerProvider p) {
                return p.getGroupnameList(siteID);
            }
        });

        List<String> groupNameList = new ArrayList<String>();

        Iterator<List<String>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            Collection<String> curResult = resultEnum.next ();
            groupNameList.addAll (curResult);
        }
        return groupNameList;
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

    /*public TreeSet getServerList (String name) {
        return (TreeSet)serversTable.get(name);
    }*/

    public List<String> getUserMembership (final JahiaUser user) {
        if (isSingleProvider()) {
            return defaultProviderInstance.getUserMembership(user);
        }
        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaGroupManagerProvider p) {
                return p.getUserMembership(user);
            }
        });
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

    public boolean groupExists (final int siteID, final String name) {
        if (isSingleProvider()) {
            return defaultProviderInstance.groupExists(siteID, name);
        }
        if(getJahiaJcrEnforcedGroups().contains(name)) {
            return getProvider(jahiaJcrEnforcedGroupsProviderKey).groupExists(siteID, name)?Boolean.TRUE:Boolean.FALSE;
        }
        Boolean result = routeCallAllUntilSuccess(new Command<Boolean>() {
            public Boolean execute(JahiaGroupManagerProvider p) {
                return p.groupExists(siteID, name) ? Boolean.TRUE : null;
            }
        });
        
        return result != null && result.booleanValue();
    }

    public JahiaGroup lookupGroup (final String groupKey) {
        if (isSingleProvider()) {
            return defaultProviderInstance.lookupGroup(groupKey);
        }
        if(getJahiaJcrEnforcedGroups().contains(StringUtils.substringBefore(groupKey,":"))) {
            return getProvider(jahiaJcrEnforcedGroupsProviderKey).lookupGroup(groupKey);
        }
        return routeCallAllUntilSuccess(new Command<JahiaGroup>() {
            public JahiaGroup execute(JahiaGroupManagerProvider p) {
                return p.lookupGroup(groupKey);
            }
        });
    }

    public JahiaGroup lookupGroup (final int siteID, final String name) {
        if (isSingleProvider()) {
            return defaultProviderInstance.lookupGroup(siteID, name);
        }
        if(getJahiaJcrEnforcedGroups().contains(name)) {
            return getProvider(jahiaJcrEnforcedGroupsProviderKey).lookupGroup(siteID,name);
        }
        return routeCallAllUntilSuccess(new Command<JahiaGroup>() {
            public JahiaGroup execute(JahiaGroupManagerProvider p) {
                return p.lookupGroup(siteID, name);
            }
        });
    }

    public boolean removeUserFromAllGroups (final JahiaUser user) {
        if (isSingleProvider()) {
            return defaultProviderInstance.removeUserFromAllGroups(user);
        }
        List<Boolean> resultList = routeCallAll(new Command<Boolean>() {
            public Boolean execute(JahiaGroupManagerProvider p) {
                return p.removeUserFromAllGroups(user);
            }
        });

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
        if (isSingleProvider()) {
            return defaultProviderInstance.searchGroups(siteID, searchCriterias);
        }
        Set<JahiaGroup> groupList = new HashSet<JahiaGroup>();

        List<Set<JahiaGroup>> resultList = routeCallAll(new Command<Set<JahiaGroup>>() {
            public Set<JahiaGroup> execute(JahiaGroupManagerProvider p) {
                return p.searchGroups(siteID, searchCriterias);
            }
        });
        
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
        if (defaultProviderInstance == null) {
            return Collections.emptySet();
        }
        final JahiaGroupManagerProvider providerInstance;
        if (isSingleProvider() || providerKey == null || (providerInstance = (JahiaGroupManagerProvider) providersTable.get(providerKey)) == null) {
            return defaultProviderInstance.searchGroups(siteID, searchCriterias);
        }
        if (useProviderClassLoader(providerInstance)) {
            return ClassLoaderUtils.executeWith(providerInstance.getClass().getClassLoader(), new Callback<Set<JahiaGroup>>() {
                @Override
                public Set<JahiaGroup> execute() {
                    return providerInstance.searchGroups(siteID, searchCriterias);
                }
            });
        } else {
            return providerInstance.searchGroups(siteID, searchCriterias);
        }
    }

    /**
     * Returns <code>true</code> in case the calls to that provider should be executed using provider class loader.
     * 
     * @param p
     *            the provider to check
     * @return <code>true</code> in case the calls to that provider should be executed using provider class loader; <code>false</code> if
     *         current (thread context) class loader should be used
     */
    private boolean useProviderClassLoader(final JahiaGroupManagerProvider p) {
        return !(p instanceof JCRGroupManagerProvider);
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public void updateCache(final JahiaGroup jahiaGroup) {
        if (defaultProviderInstance != null) {
            defaultProviderInstance.updateCache(jahiaGroup);
        }
    }

    private <T> T routeCallAllUntilSuccess(final Command<T> v) {
        // we're calling the providers in order, until one returns
        // a success condition (to be defined)

        T result = null;
        for (final JahiaGroupManagerProvider p : providersTable.values()) {
            if (useProviderClassLoader(p)) {
                result = ClassLoaderUtils.executeWith(p.getClass().getClassLoader(), new Callback<T>() {
                    @Override
                    public T execute() {
                        return v.execute(p);
                    }
                });
            } else {
                result = v.execute(p);
            }
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    private <T> List<T> routeCallAll(final Command<T> v) {
        // we're calling all the providers
        List<T> results = new ArrayList<T>();

        for (final JahiaGroupManagerProvider p : providersTable.values()) {
            if (useProviderClassLoader(p)) {
                results.add(ClassLoaderUtils.executeWith(p.getClass().getClassLoader(), new Callback<T>() {
                    @Override
                    public T execute() {
                        return v.execute(p);
                    }
                }));
            } else {
                results.add(v.execute(p));
            }
        }

        return results;
    }

    public boolean isGroupNameSyntaxCorrect(final String name) {
        return defaultProviderInstance != null && defaultProviderInstance.isGroupNameSyntaxCorrect(name);
    }

    public void setJahiaJcrEnforcedGroups(List<String> jahiaJcrEnforcedGroups) {
        this.jahiaJcrEnforcedGroups = jahiaJcrEnforcedGroups;
    }

    public List<String> getJahiaJcrEnforcedGroups() {
        return jahiaJcrEnforcedGroups;
    }

    public void setJahiaJcrEnforcedGroupsProviderKey(String jahiaJcrEnforcedGroupsProviderKey) {
        this.jahiaJcrEnforcedGroupsProviderKey = jahiaJcrEnforcedGroupsProviderKey;
    }

    public String getJahiaJcrEnforcedGroupsProviderKey() {
        return jahiaJcrEnforcedGroupsProviderKey;
    }

    interface Command<T> {
        T execute(JahiaGroupManagerProvider p);
    }

    @Override
    public void registerProvider(JahiaGroupManagerProvider provider) {
        logger.info("Registered group provider {}", provider.getKey());
        providersTable.put(provider.getKey(), provider);
        sortedProviders.add(provider);
        if (defaultProviderInstance == null || provider.isDefaultProvider()) {
            defaultProviderInstance = provider;
        }
        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new ProviderEvent(provider.getKey()));
        }
    }

    @Override
    public void unregisterProvider(JahiaGroupManagerProvider provider) {
        logger.info("Unregistering group provider {}", provider.getKey());
        providersTable.remove(provider.getKey());
        sortedProviders.remove(provider);
        if (provider.isDefaultProvider() && defaultProviderInstance == provider) {
            for (JahiaGroupManagerProvider p : sortedProviders) {
                if (p.isDefaultProvider()) {
                    defaultProviderInstance = p;
                    break;
                }
            }
            if (defaultProviderInstance == null && !sortedProviders.isEmpty()) {
                defaultProviderInstance = sortedProviders.iterator().next();
            }
        }
    }

    @Override
    public void flushCache() {
        if (isSingleProvider()) {
            defaultProviderInstance.flushCache();
            return;
        }
        routeCallAll(new Command<Boolean>() {
            public Boolean execute(JahiaGroupManagerProvider p) {
                p.flushCache();
                return Boolean.TRUE;
            }
        });
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

    private int getSiteId(final String siteKey) {
        try {
            if (siteKey != null) {
                return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
                    @Override
                    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        return ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(siteKey , session).getID();
                    }
                });
            }
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public JahiaGroup createGroup(String siteKey, String name, Properties properties, boolean hidden) {
         return createGroup(getSiteId(siteKey), name, properties, hidden);
    }

    @Override
    public JahiaGroup getAdministratorGroup(String siteKey) {
        return getAdministratorGroup(getSiteId(siteKey));
    }

    @Override
    public List<String> getGroupList(String siteKey) {
        return getGroupList(getSiteId(siteKey));
    }

    @Override
    public List<String> getGroupnameList(String siteKey) {
        return getGroupnameList(getSiteId(siteKey));
    }

    @Override
    public boolean groupExists(String siteKey, String name) {
        return groupExists(getSiteId(siteKey), name);
    }

    @Override
    public JahiaGroup lookupGroup(String siteKey, String name) {
        return lookupGroup(getSiteId(siteKey), name);
    }

    @Override
    public Set<JahiaGroup> searchGroups(String siteKey, Properties searchCriterias) {
        return searchGroups(getSiteId(siteKey), searchCriterias);
    }

    @Override
    public Set<JahiaGroup> searchGroups(String providerKey, String siteKey, Properties searchCriterias) {
        return searchGroups(providerKey,getSiteId(siteKey), searchCriterias);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
