/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.usermanager;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Implementation of the group manager service which delegates the calls to multiple registered group providers.
 */
public class JahiaGroupManagerRoutingService extends JahiaGroupManagerService implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(JahiaGroupManagerRoutingService.class);

    static private JahiaGroupManagerRoutingService mInstance = new JahiaGroupManagerRoutingService();

    private static final Comparator<JahiaGroupManagerProvider> PROVIDER_COMPARATOR = new Comparator<JahiaGroupManagerProvider>() {
        public int compare(JahiaGroupManagerProvider o1, JahiaGroupManagerProvider o2) {
            return o1.getPriority() - o2.getPriority() != 0 ? o1.getPriority() - o2.getPriority() : o1.getKey()
                    .compareTo(o2.getKey());
        }
    };

    private SelfPopulatingCache siteKeyIdMap;

    private Map<String, JahiaGroupManagerProvider> providerMap = new HashMap<String, JahiaGroupManagerProvider>();
    private List<JahiaGroupManagerProvider> providers = Collections.emptyList();
    private JahiaGroupManagerProvider defaultProvider = null;
    private List<String> jahiaJcrEnforcedGroups;
    private String jahiaJcrEnforcedGroupsProviderKey;

    private ApplicationEventPublisher applicationEventPublisher;
    private EhCacheProvider ehCacheProvider;

    /**
     * Create an new instance of the Group Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return the instance of the Group Manager Service
     */
    public static JahiaGroupManagerRoutingService getInstance() {
        return mInstance;
    }

    public void start() throws JahiaInitializationException {
        // do nothing
    }

    public void stop() throws JahiaException {
        // do nothing
    }

    public JahiaGroup createGroup(final int siteID, final String name, final Properties properties, final boolean hidden) {
        return defaultProvider != null ? defaultProvider.createGroup(siteID, name, properties, hidden) : null;
    }

    public boolean deleteGroup(final JahiaGroup group) {
        return group != null && (defaultProvider != null ? defaultProvider.deleteGroup(group) : false);
    }

    public List<JahiaSite> getAdminGrantedSites(final JahiaUser user) throws JahiaException {
        if (isSingleProvider()) {
            return defaultProvider.getAdminGrantedSites(user);
        }

        Set<JahiaSite> sitesList = new LinkedHashSet<JahiaSite>();
        for (JahiaGroupManagerProvider p : providers) {
            List<JahiaSite> adminGrantedSites = p.getAdminGrantedSites(user);
            if (adminGrantedSites != null) {
                sitesList.addAll(adminGrantedSites);
            }
        }

        return new LinkedList<JahiaSite>(sitesList);
    }

    private boolean isSingleProvider() {
        return providers.size() == 1;
    }

    public JahiaGroup getAdministratorGroup(final int siteID) {
        if (isSingleProvider()) {
            return defaultProvider.getAdministratorGroup(siteID);
        }
        JahiaGroup group = null;
        for (JahiaGroupManagerProvider p : providers) {
            group = p.getAdministratorGroup(siteID);
            if (group != null) {
                break;
            }
        }

        return group;
    }

    public List<String> getGroupList() {
        if (isSingleProvider()) {
            return defaultProvider.getGroupList();
        }
        List<String> groupList = new ArrayList<String>();

        for (JahiaGroupManagerProvider p : providers) {
            groupList.addAll(p.getGroupList());
        }

        return groupList;
    }

    public List<String> getGroupList(final int siteID) {
        if (isSingleProvider()) {
            return defaultProvider.getGroupList(siteID);
        }
        List<String> groupList = new ArrayList<String>();
        for (JahiaGroupManagerProvider p : providers) {
            List<String> curResult = p.getGroupList(siteID);
            if (curResult != null) {
                groupList.addAll(curResult);
            }
        }

        return groupList;
    }

    public List<String> getGroupnameList() {
        if (isSingleProvider()) {
            return defaultProvider.getGroupnameList();
        }
        List<String> groupNameList = new ArrayList<String>();
        for (JahiaGroupManagerProvider p : providers) {
            groupNameList.addAll(p.getGroupnameList());
        }

        return groupNameList;
    }

    public List<String> getGroupnameList(final int siteID) {
        if (isSingleProvider()) {
            return defaultProvider.getGroupnameList(siteID);
        }
        List<String> groupNameList = new ArrayList<String>();
        for (JahiaGroupManagerProvider p : providers) {
            groupNameList.addAll(p.getGroupnameList(siteID));
        }

        return groupNameList;
    }

    /**
     * Returns a List of JahiaGroupManagerProvider object describing the
     * available group management providers
     *
     * @return result a List of JahiaGroupManagerProvider objects that describe
     * the providers. This will never be null but may be empty if no providers
     * are available.
     */
    public List<? extends JahiaGroupManagerProvider> getProviderList() {
        return providers;
    }

    public List<String> getUserMembership(final JahiaUser user) {
        if (isSingleProvider()) {
            return defaultProvider.getUserMembership(user);
        }

        List<String> userMembership = new ArrayList<String>();
        for (JahiaGroupManagerProvider p : providers) {
            List<String> curResult = p.getUserMembership(user);
            if (curResult != null) {
                userMembership.addAll(curResult);
            }
        }

        return userMembership;
    }

    public boolean groupExists(final int siteID, final String name) {
        if (isSingleProvider()) {
            return defaultProvider.groupExists(siteID, name);
        }
        if (getJahiaJcrEnforcedGroups().contains(name)) {
            return getProvider(jahiaJcrEnforcedGroupsProviderKey).groupExists(siteID, name) ? Boolean.TRUE : Boolean.FALSE;
        }
        boolean result = false;
        for (JahiaGroupManagerProvider p : providers) {
            result = p.groupExists(siteID, name);
            if (result) {
                break;
            }
        }

        return result;
    }

    public JahiaGroup lookupGroup(final String groupKey) {
        if (isSingleProvider()) {
            return defaultProvider.lookupGroup(groupKey);
        }
        if (getJahiaJcrEnforcedGroups().contains(StringUtils.substringBefore(groupKey, ":"))) {
            return getProvider(jahiaJcrEnforcedGroupsProviderKey).lookupGroup(groupKey);
        }
        JahiaGroup group = null;
        for (JahiaGroupManagerProvider p : providers) {
            group = p.lookupGroup(groupKey);
            if (group != null) {
                break;
            }
        }

        return group;
    }

    public JahiaGroup lookupGroup(final int siteID, final String name) {
        if (isSingleProvider()) {
            return defaultProvider.lookupGroup(siteID, name);
        }
        if (getJahiaJcrEnforcedGroups().contains(name)) {
            return getProvider(jahiaJcrEnforcedGroupsProviderKey).lookupGroup(siteID, name);
        }
        JahiaGroup group = null;
        for (JahiaGroupManagerProvider p : providers) {
            group = p.lookupGroup(siteID, name);
            if (group != null) {
                break;
            }
        }

        return group;
    }

    public boolean removeUserFromAllGroups(final JahiaUser user) {
        if (isSingleProvider()) {
            return defaultProvider.removeUserFromAllGroups(user);
        }
        boolean success = true;
        for (JahiaGroupManagerProvider p : providers) {
            success = p.removeUserFromAllGroups(user) && success;
        }

        return success;
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criteria
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*")
     * @return Set a set of JahiaGroup elements that correspond to those
     * search criteria
     */
    public Set<JahiaGroup> searchGroups(final int siteID, final Properties searchCriterias) {
        if (isSingleProvider()) {
            return defaultProvider.searchGroups(siteID, searchCriterias);
        }
        Set<JahiaGroup> groupList = new HashSet<JahiaGroup>();

        for (JahiaGroupManagerProvider p : providers) {
            groupList.addAll(p.searchGroups(siteID, searchCriterias));
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
     * @param searchCriterias a Properties object that contains search criteria
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criteria
     * @return Set a set of JahiaGroup elements that correspond to those
     * search criteria
     */
    public Set<JahiaGroup> searchGroups(final String providerKey, final int siteID, final Properties searchCriterias) {
        if (defaultProvider == null) {
            return Collections.emptySet();
        }
        final JahiaGroupManagerProvider p;
        if (isSingleProvider() || providerKey == null || (p = providerMap.get(providerKey)) == null) {
            return defaultProvider.searchGroups(siteID, searchCriterias);
        }
        return p.searchGroups(siteID, searchCriterias);
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     *
     * @param jahiaGroup JahiaGroup the group to be updated in the cache.
     */
    public void updateCache(final JahiaGroup jahiaGroup) {
        for (JahiaGroupManagerProvider p : providers) {
            if (p.isDefaultProvider() || p.getKey().equals(jahiaGroup.getProviderName())) {
                p.updateCache(jahiaGroup);
            }
        }
    }

    public boolean isGroupNameSyntaxCorrect(final String name) {
        return defaultProvider != null && defaultProvider.isGroupNameSyntaxCorrect(name);
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

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void registerProvider(JahiaGroupManagerProvider provider) {
        logger.info("Registered group provider {}", provider.getKey());

        List<JahiaGroupManagerProvider> newProviderList = null;
        if (!providers.isEmpty()) {
            Set<JahiaGroupManagerProvider> newProviderSet = new TreeSet<JahiaGroupManagerProvider>(PROVIDER_COMPARATOR);
            newProviderSet.addAll(providers);
            newProviderSet.add(provider);
            newProviderList = new LinkedList<JahiaGroupManagerProvider>(newProviderSet);
        } else {
            newProviderList = new LinkedList<JahiaGroupManagerProvider>();
            newProviderList.add(provider);
        }
        providers = UnmodifiableList.decorate(newProviderList);
        providerMap.put(provider.getKey(), provider);

        if (defaultProvider == null || provider.isDefaultProvider()) {
            defaultProvider = provider;
        }
        if (applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(new ProviderEvent(provider.getKey()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void unregisterProvider(JahiaGroupManagerProvider provider) {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        logger.info("Unregistering group provider {}", provider.getKey());

        if (providerMap.remove(provider.getKey()) != null) {
            if (isSingleProvider()) {
                providers = Collections.emptyList();
            } else {
                Set<JahiaGroupManagerProvider> newProviderSet = new TreeSet<JahiaGroupManagerProvider>(PROVIDER_COMPARATOR);
                newProviderSet.addAll(providers);
                newProviderSet.remove(provider);
                providers = UnmodifiableList.decorate(new LinkedList<JahiaGroupManagerProvider>(newProviderSet));
            }
        }
        if (provider.isDefaultProvider() && defaultProvider == provider) {
            for (JahiaGroupManagerProvider p : providers) {
                if (p.isDefaultProvider()) {
                    defaultProvider = p;
                    break;
                }
            }
            if (defaultProvider == null && !providers.isEmpty()) {
                defaultProvider = providers.iterator().next();
            }
        }
    }

    @Override
    public void flushCache() {
        if (isSingleProvider()) {
            defaultProvider.flushCache();
            return;
        }
        for (JahiaGroupManagerProvider p : providers) {
            p.flushCache();
        }
    }

    public void setDefaultProvider(JahiaGroupManagerProvider defaultProvider) {
        defaultProvider.setDefaultProvider(true);
        defaultProvider.setGroupManagerService(this);
        registerProvider(defaultProvider);
    }

    @Override
    public JahiaGroupManagerProvider getProvider(String name) {
        return providerMap.get(name);
    }

    private int getSiteId(final String siteKey) {
        if (StringUtils.isNotEmpty(siteKey)) {
            if (siteKeyIdMap == null) {
                siteKeyIdMap = ehCacheProvider.registerSelfPopulatingCache("org.jahia.groups.siteKeyIDCache", new CacheEntryFactory() {
                    @Override
                    public Object createEntry(final Object key) throws Exception {
                        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
                        try {
                            return ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) key, session).getID();
                        } catch (RepositoryException e) {
                            return 0;
                        }
                    }
                });
            }
            return (Integer) siteKeyIdMap.get(siteKey).getObjectValue();
        }
        return 0;
    }

    /**
     * Flush the cache of site id by site key.
     */
    public void flushSiteKeyIdMap() {
        if (siteKeyIdMap != null) {
            siteKeyIdMap.refresh(false);
        }
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
        return searchGroups(providerKey, getSiteId(siteKey), searchCriterias);
    }

    @Override
    public synchronized void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }
}
