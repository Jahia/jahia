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

import org.apache.commons.collections.FastHashMap;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.utils.ClassLoaderUtils;
import org.jahia.utils.ClassLoaderUtils.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.security.Principal;
import java.util.*;

/**
 * <p>Title: Manages routing of user management processes to the corresponding
 * provider.</p>
 * <p>Description: This service is the heart of the "routing" process of the
 * user manager. It is also a configurable system where regexps are using to
 * define the routing process. These regexps are taken in a specific order, as
 * to give priority to one provider or another, such as : </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 3.0
 */

public class JahiaUserManagerRoutingService extends JahiaUserManagerService implements ApplicationEventPublisherAware {

    interface Command<T> {
        T execute(JahiaUserManagerProvider p);
    }

    private static transient Logger logger = LoggerFactory.getLogger(
            JahiaUserManagerRoutingService.class);

    static private JahiaUserManagerRoutingService mInstance = null;

    private Map<String, JahiaUserManagerProvider> providersTable = null;
    private Set<JahiaUserManagerProvider> sortedProviders = null;

    private JahiaUserManagerProvider defaultProviderInstance;

    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Create an new instance of the User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the User Manager Service.
     */
    public static JahiaUserManagerRoutingService getInstance () {
        if (mInstance == null) {
            mInstance = new JahiaUserManagerRoutingService ();
        }
        return mInstance;
    }

    @SuppressWarnings("unchecked")
    protected JahiaUserManagerRoutingService () {
    	FastHashMap map = new FastHashMap(2);
    	map.setFast(true);
        providersTable = map;

        sortedProviders = new TreeSet<JahiaUserManagerProvider>(new Comparator<JahiaUserManagerProvider>() {
            public int compare (JahiaUserManagerProvider o1, JahiaUserManagerProvider o2) {
                return o1.getPriority () - o2.getPriority () != 0 ? o1.getPriority () - o2.getPriority () : o1.getKey().compareTo(o2.getKey());
            }
        });
    }

    public void start() throws JahiaInitializationException {
    	// do nothing
        logger.info("User manager routing service now started.");
    }

    public void stop() throws JahiaException {
    	// nothing to do
    }


    public JahiaUser createUser(final String name,
                                final String password,
                                final Properties properties) {
        return defaultProviderInstance != null ? defaultProviderInstance.createUser(name, password, properties) : null;
    }

    public boolean deleteUser (final JahiaUser user) {
        if (user == null || defaultProviderInstance == null) {
            return false;
        }
        return defaultProviderInstance.deleteUser(user);
    }

    public int getNbUsers () throws JahiaException {
        if (isSingleProvider()) {
            return defaultProviderInstance.getNbUsers();
        }
        List<Integer> resultList = routeCallAll(new Command<Integer>() {
            public Integer execute(JahiaUserManagerProvider p) {
                return p.getNbUsers();
            }
        });
        int nbUsers = 0;
        for (Integer c : resultList) {
            nbUsers += c;
        }
        
        return nbUsers;
    }

    public List<String> getUserList () {
        if (isSingleProvider()) {
            return defaultProviderInstance.getUserList();
        }
        
        List<String> userList = new ArrayList<String>();

        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaUserManagerProvider p) {
                return p.getUserList();
            }
        });
        
        for (List<String> l : resultList) {
            userList.addAll(l);
        }

        return userList;
    }

    public List<String> getUserList (String provider) {
        return new ArrayList<String>((providersTable.get(provider)).getUserList());
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
        return new ArrayList<JahiaUserManagerProvider>(sortedProviders);
    }

    public List<String> getUsernameList() {
        if (isSingleProvider()) {
            return defaultProviderInstance.getUsernameList();
        }
        
        List<String> userNameList = new ArrayList<String>();

        List<List<String>> resultList = routeCallAll(new Command<List<String>>() {
            public List<String> execute(JahiaUserManagerProvider p) {
                return p.getUsernameList();
            }
        });
        
        for (List<String> l : resultList) {
            userNameList.addAll(l);
        }

        return userNameList;
    }

    public JahiaUser lookupUserByKey(final String userKey) {
        if (isSingleProvider()) {
            return defaultProviderInstance.lookupUserByKey(userKey);
        }
        return routeCallAllUntilSuccess(new Command<JahiaUser>() {
            public JahiaUser execute(JahiaUserManagerProvider p) {
                return p.lookupUserByKey(userKey);
            }
        });
    }

    public JahiaUser lookupUser(final String name) {
        if (isSingleProvider()) {
            return defaultProviderInstance.lookupUser(name);
        }
        return routeCallAllUntilSuccess(new Command<JahiaUser>() {
            public JahiaUser execute(JahiaUserManagerProvider p) {
                return p.lookupUser(name);
            }
        });
    }

    /**
     * Find users according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*")
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     */
    public Set<Principal> searchUsers (final Properties searchCriterias) {
        if (isSingleProvider()) {
            return new LinkedHashSet<Principal>(defaultProviderInstance.searchUsers(searchCriterias));
        }
           
        Set<Principal> userList = new LinkedHashSet<Principal>();

        List<Set<JahiaUser>> resultList = routeCallAll(new Command<Set<JahiaUser>>() {
            public Set<JahiaUser> execute(JahiaUserManagerProvider p) {
                return p.searchUsers(searchCriterias);
            }
        });

        Iterator<Set<JahiaUser>> resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            userList.addAll(resultEnum.next ());
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
     * @param siteID          site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "username"="*test*") or
     *                        null to search without criterias
     *
     * @return Set a set of JahiaUser elements that correspond to those
     *         search criterias
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set searchUsers(String providerKey, final Properties searchCriterias) {
        if (defaultProviderInstance == null) {
            return Collections.emptySet();
        }
        final JahiaUserManagerProvider p;
        if (isSingleProvider() || providerKey == null || (p = providersTable.get(providerKey)) == null) {
            return defaultProviderInstance.searchUsers(searchCriterias);
        }

        if (useProviderClassLoader(p)) {
            return ClassLoaderUtils.executeWith(p.getClass().getClassLoader(), new Callback<Set<JahiaUser>>() {
                @Override
                public Set<JahiaUser> execute() {
                    return p.searchUsers(searchCriterias);
                }
            });
        } else {
            return p.searchUsers(searchCriterias);
        }
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaUser JahiaUser the user to be updated in the cache.
     */
    public void updateCache(final JahiaUser jahiaUser) {
        if (defaultProviderInstance != null) {
            defaultProviderInstance.updateCache(jahiaUser);
        }
    }

    public boolean userExists(final String name) {
        if (isSingleProvider()) {
            return defaultProviderInstance.userExists(name);
        }
        Boolean result = routeCallAllUntilSuccess(new Command<Boolean>() {
            public Boolean execute(JahiaUserManagerProvider p) {
                return p.userExists(name) ? Boolean.TRUE : null;
            }
        });
        
        return result != null && result.booleanValue();
    }

    private <T> T routeCallAllUntilSuccess(final Command<T> v) {
        // we're calling the providers in order, until one returns
        // a success condition

        T result = null;
        for (final JahiaUserManagerProvider curProvider : providersTable.values()) {
            if (useProviderClassLoader(curProvider)) {
                result = ClassLoaderUtils.executeWith(curProvider.getClass().getClassLoader(), new Callback<T>() {
                    @Override
                    public T execute() {
                        return v.execute(curProvider);
                    }
                });
            } else {
                result = v.execute(curProvider);
            }
            if (result != null) {
                return result;
            }
        }

        return result;
    }

    private <T> List<T> routeCallAll(final Command<T> v) {
        // we're calling all the providers
        List<T> results = new ArrayList<T> ();
        
        for (final JahiaUserManagerProvider curProvider : providersTable.values()) {
            if (useProviderClassLoader(curProvider)) {
                results.add(ClassLoaderUtils.executeWith(curProvider.getClass().getClassLoader(), new Callback<T>() {
                    @Override
                    public T execute() {
                        return v.execute(curProvider);
                    }
                }));
            } else {
                results.add(v.execute(curProvider));
            }
        }

        return results;
    }

    public boolean isUsernameSyntaxCorrect(final String name) {
        return defaultProviderInstance != null && defaultProviderInstance.isUsernameSyntaxCorrect(name);
    }

    @Override
    public void registerProvider(JahiaUserManagerProvider provider) {
        logger.info("Registering user provider {}", provider.getKey());
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
    public void unregisterProvider(JahiaUserManagerProvider provider) {
        logger.info("Unregistering user provider {}", provider.getKey());
        providersTable.remove(provider.getKey());
        sortedProviders.remove(provider);
        if (provider.isDefaultProvider() && defaultProviderInstance == provider) {
            for (JahiaUserManagerProvider p : sortedProviders) {
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

    public void setDefaultProvider(JahiaUserManagerProvider defaultProvider) {
    	defaultProvider.setDefaultProvider(true);
    	defaultProvider.setUserManagerService(this);
    	registerProvider(defaultProvider);
    }

    private boolean isSingleProvider() {
        return providersTable.size() == 1;
    }

    @Override
    public JahiaUserManagerProvider getProvider(String name) {
        return providersTable.get(name);
    }
    
    /**
     * Returns <code>true</code> in case the calls to that provider should be executed using provider class loader.
     * 
     * @param p
     *            the provider to check
     * @return <code>true</code> in case the calls to that provider should be executed using provider class loader; <code>false</code> if
     *         current (thread context) class loader should be used
     */
    private boolean useProviderClassLoader(final JahiaUserManagerProvider p) {
        return !(p instanceof JCRUserManagerProvider);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
