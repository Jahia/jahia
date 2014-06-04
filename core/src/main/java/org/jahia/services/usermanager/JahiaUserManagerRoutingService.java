/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.usermanager;

import org.apache.commons.collections.list.UnmodifiableList;
import org.drools.core.util.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.security.Principal;
import java.util.*;

/**
 * Manages routing of user management processes to the corresponding provider.
 * 
 * @author Serge Huber
 */
public class JahiaUserManagerRoutingService extends JahiaUserManagerService implements ApplicationEventPublisherAware {

    private static final Comparator<JahiaUserManagerProvider> PROVIDER_COMPARATOR = new Comparator<JahiaUserManagerProvider>() {
        public int compare(JahiaUserManagerProvider o1, JahiaUserManagerProvider o2) {
            return o1.getPriority() - o2.getPriority() != 0 ? o1.getPriority() - o2.getPriority() : o1.getKey()
                    .compareTo(o2.getKey());
        }
    };

    private static transient Logger logger = LoggerFactory.getLogger(JahiaUserManagerRoutingService.class);

    static private JahiaUserManagerRoutingService mInstance = new JahiaUserManagerRoutingService();

    private Map<String, JahiaUserManagerProvider> providerMap = new HashMap<String, JahiaUserManagerProvider>(2);
    
    private List<JahiaUserManagerProvider> providers = Collections.emptyList();

    private JahiaUserManagerProvider defaultProvider;

    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return the instance of the User Manager Service
     */
    public static JahiaUserManagerRoutingService getInstance () {
        return mInstance;
    }

    public void start() throws JahiaInitializationException {
    	// do nothing
    }

    public void stop() throws JahiaException {
    	// nothing to do
    }


    public JahiaUser createUser(final String name,
                                final String password,
                                final Properties properties) {
        return defaultProvider != null ? defaultProvider.createUser(name, password, properties) : null;
    }

    public boolean deleteUser (final JahiaUser user) {
        if (user == null || defaultProvider == null) {
            return false;
        }
        return defaultProvider.deleteUser(user);
    }

    public int getNbUsers () throws JahiaException {
        if (isSingleProvider()) {
            return defaultProvider.getNbUsers();
        }
        int nbUsers = 0;
        for (JahiaUserManagerProvider p : providers) {
            nbUsers += p.getNbUsers();
        }
        
        return nbUsers;
    }

    public List<String> getUserList () {
        if (isSingleProvider()) {
            return defaultProvider.getUserList();
        }
        
        List<String> userList = new ArrayList<String>();

        for (JahiaUserManagerProvider p : providers) {
            userList.addAll(p.getUserList());
        }

        return userList;
    }

    public List<String> getUserList(String provider) {
        return providerMap.get(provider).getUserList();
    }

    /**
     * Returns a List of JahiaUserManagerProvider object describing the
     * available user management providers
     *
     * @return result a List of JahiaUserManagerProvider objects that describe
     *         the providers. This will never be null but may be empty if no providers
     *         are available.
     */
    public List<JahiaUserManagerProvider> getProviderList () {
        return providers;
    }

    public List<String> getUsernameList() {
        if (isSingleProvider()) {
            return defaultProvider.getUsernameList();
        }
        
        List<String> userNameList = new ArrayList<String>();
        
        for (JahiaUserManagerProvider p : providers) {
            userNameList.addAll(p.getUsernameList());
        }

        return userNameList;
    }

    public JahiaUser lookupUserByKey(final String userKey) {
        if(StringUtils.isEmpty(userKey)){
            return null;
        }
        if (isSingleProvider()) {
            return defaultProvider.lookupUserByKey(userKey);
        }
        JahiaUser user = null;
        for (JahiaUserManagerProvider p : providers) {
            user = p.lookupUserByKey(userKey);
            if (user != null) {
                break;
            }
        }
        
        return user;
    }

    public JahiaUser lookupUser(final String name) {
        if(StringUtils.isEmpty(name)){
            return null;
        }
        if (isSingleProvider()) {
            return defaultProvider.lookupUser(name);
        }
        JahiaUser user = null;
        for (JahiaUserManagerProvider p : providers) {
            user = p.lookupUser(name);
            if (user != null) {
                break;
            }
        }
        
        return user;
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param searchcriteria a Properties object that contains search criteria
     *                        in the format name,value (for example "*"="*" or "username"="*test*")
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criteria
     */
    public Set<Principal> searchUsers (final Properties searchcriteria) {
        if (isSingleProvider()) {
            return new LinkedHashSet<Principal>(defaultProvider.searchUsers(searchcriteria));
        }
           
        Set<Principal> userList = new LinkedHashSet<Principal>();

        for (JahiaUserManagerProvider p : providers) {
            userList.addAll(p.searchUsers(searchcriteria));
        }
        return userList;
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param providerKey     key of the provider in which to search, may be
     *                        obtained by calling getProviderList()
     * @param searchcriteria a Properties object that contains search criteria
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criteria
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criteria
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set searchUsers(String providerKey, final Properties searchcriteria) {
        if (defaultProvider == null) {
            return Collections.emptySet();
        }
        final JahiaUserManagerProvider p;
        if (isSingleProvider() || providerKey == null || (p = providerMap.get(providerKey)) == null) {
            return defaultProvider.searchUsers(searchcriteria);
        }

        return p.searchUsers(searchcriteria);
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaUser JahiaUser the user to be updated in the cache.
     */
    public void updateCache(final JahiaUser jahiaUser) {
        for (JahiaUserManagerProvider p : providers) {
            if (p.isDefaultProvider() || p.getKey().equals(jahiaUser.getProviderName())) {
                p.updateCache(jahiaUser);
            }
        }
    }

    public boolean userExists(final String name) {
        if (isSingleProvider()) {
            return defaultProvider.userExists(name);
        }
        boolean result = false;
        for (JahiaUserManagerProvider p : providers) {
            result = p.userExists(name);
            if (result) {
                break;
            }
        }
        
        return result;
    }

    public boolean isUsernameSyntaxCorrect(final String name) {
        return defaultProvider != null && defaultProvider.isUsernameSyntaxCorrect(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void registerProvider(JahiaUserManagerProvider provider) {
        logger.info("Registering user provider {}", provider.getKey());
        
        List<JahiaUserManagerProvider> newProviderList = null;
        if (!providers.isEmpty()) {
            Set<JahiaUserManagerProvider> newProviderSet = new TreeSet<JahiaUserManagerProvider>(PROVIDER_COMPARATOR);
            newProviderSet.addAll(providers);
            newProviderSet.add(provider);
            newProviderList = new LinkedList<JahiaUserManagerProvider>(newProviderSet);
        } else {
            newProviderList = new LinkedList<JahiaUserManagerProvider>();
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
    public synchronized void unregisterProvider(JahiaUserManagerProvider provider) {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        logger.info("Unregistering user provider {}", provider.getKey());
        
        if (providerMap.remove(provider.getKey()) != null) {
            if (isSingleProvider()) {
                providers = Collections.emptyList();
            } else {
                Set<JahiaUserManagerProvider> newProviderSet = new TreeSet<JahiaUserManagerProvider>(PROVIDER_COMPARATOR);
                newProviderSet.addAll(providers);
                newProviderSet.remove(provider);
                providers = UnmodifiableList.decorate(new LinkedList<JahiaUserManagerProvider>(newProviderSet));
            }
        }
        if (provider.isDefaultProvider() && defaultProvider == provider) {
            for (JahiaUserManagerProvider p : providers) {
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

    public void setDefaultProvider(JahiaUserManagerProvider defaultProvider) {
    	defaultProvider.setDefaultProvider(true);
    	defaultProvider.setUserManagerService(this);
    	registerProvider(defaultProvider);
    }

    private boolean isSingleProvider() {
        return providers.size() == 1;
    }

    @Override
    public JahiaUserManagerProvider getProvider(String name) {
        return providerMap.get(name);
    }

    @Override
    public synchronized void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
