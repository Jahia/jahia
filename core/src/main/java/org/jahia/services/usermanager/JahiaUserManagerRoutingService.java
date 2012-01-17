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

import org.apache.commons.collections.FastHashMap;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;

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

public class JahiaUserManagerRoutingService extends JahiaUserManagerService {

    private static transient org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(
            JahiaUserManagerRoutingService.class);

// ------------------------------ FIELDS ------------------------------

    static private JahiaUserManagerRoutingService mInstance = null;

    private Map<String, JahiaUserManagerProvider> providersTable = null;
    private Set<JahiaUserManagerProvider> sortedProviders = null;

	private JahiaUserManagerProvider defaultProviderInstance;

// -------------------------- STATIC METHODS --------------------------

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

// --------------------------- CONSTRUCTORS ---------------------------

    @SuppressWarnings("unchecked")
    protected JahiaUserManagerRoutingService () {
    	FastHashMap map = new FastHashMap(2);
    	map.setFast(true);
        providersTable = map;

        sortedProviders = new TreeSet<JahiaUserManagerProvider>(new Comparator<JahiaUserManagerProvider>() {
            public int compare (JahiaUserManagerProvider o1, JahiaUserManagerProvider o2) {
                return o1.getPriority () - o2.getPriority ();
            }
        });
    }

// -------------------------- OTHER METHODS --------------------------

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
        JahiaUser user = routeCallOne(new Command<JahiaUser>() {
            public JahiaUser execute(JahiaUserManagerProvider p) {
                return p.createUser(name, password, properties);
            }
        }, null, properties);

        return user;
    }

    public boolean deleteUser (final JahiaUser user) {
        if (user == null) {
            return false;
        }
        Boolean resultBool = routeCallOne(new Command<Boolean>() {
            public Boolean execute(JahiaUserManagerProvider p) {
                return p.deleteUser(user);
            }
        }, null, user.getProperties());

        return resultBool;
    }

    public int getNbUsers ()
            throws org.jahia.exceptions.JahiaException {
        List resultList = routeCallAll(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                return new Integer(p.getNbUsers());
            }
        }, null);

        Iterator resultEnum = resultList.iterator();
        int nbUsers = 0;
        while (resultEnum.hasNext ()) {
            Integer curResult = (Integer) resultEnum.next ();
            nbUsers += curResult.intValue ();
        }
        return nbUsers;
    }

    public List getUserList () {
        List userList = new ArrayList();

        List resultList = routeCallAll(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                return p.getUserList();
            }
        }, null);

        Iterator resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List curResult = (List) resultEnum.next ();
            userList.addAll (curResult);
        }
        return userList;
    }

    public List<String> getUserList (String provider) {
        return new ArrayList<String>(((JahiaUserManagerProvider) providersTable.get(provider)).getUserList());
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
    private List getProvidersToCall (List providersToCall) {
        // we use this temporary List in order to avoid having twice
        // the same code for the dispatching...
        List tmpProviderInstances = new ArrayList();

        if (providersToCall != null) {
            if (providersToCall.size () >= 1) {
                Iterator providerEnum = providersToCall.iterator();
                while (providerEnum.hasNext ()) {
                    Object curProviderEntry = providerEnum.next ();
                    if (curProviderEntry instanceof String) {
                        String curProviderKey = (String) curProviderEntry;
                        JahiaUserManagerProvider curProviderInstance =
                                (JahiaUserManagerProvider) providersTable.get (curProviderKey);
                        if (curProviderInstance != null) {
                            tmpProviderInstances.add (curProviderInstance);
                        }
                    }
                }
            }
        }

        if (tmpProviderInstances.size () == 0) {
            tmpProviderInstances = new ArrayList();
            Iterator providerIter = sortedProviders.iterator ();
            while (providerIter.hasNext ()) {
                JahiaUserManagerProvider curProvider = (JahiaUserManagerProvider) providerIter.next ();
                tmpProviderInstances.add (curProvider);
            }
        }

        return tmpProviderInstances;
    }

//    public TreeSet getServerList (String name) {
//        return (TreeSet)serversTable.get(name);
//    }

    public List getUsernameList() {
        List userNameList = new ArrayList();

        List resultList = routeCallAll(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                return p.getUsernameList();
            }
        }, null);

        Iterator resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            List curResult = (List) resultEnum.next ();
            userNameList.addAll (curResult);
        }
        return userNameList;
    }

    public JahiaUser lookupUserByKey(final String userKey) {
        return routeCallAllUntilSuccess(new Command<JahiaUser>() {
            public JahiaUser execute(JahiaUserManagerProvider p) {
                return p.lookupUserByKey(userKey);
            }
        }, null);
    }

    public JahiaUser lookupUser(final String name) {
        return routeCallAllUntilSuccess(new Command<JahiaUser>() {
            public JahiaUser execute(JahiaUserManagerProvider p) {
                return p.lookupUser(name);
            }
        }, null);
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
    public Set searchUsers (final Properties searchCriterias) {
        Set userList = new HashSet();

        List resultList = routeCallAll(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                return p.searchUsers(searchCriterias);
            }
        }, null);

        Iterator resultEnum = resultList.iterator();
        while (resultEnum.hasNext ()) {
            Set curResult = (Set) resultEnum.next ();
            userList.addAll (curResult);
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
    public Set searchUsers (String providerKey,final Properties searchCriterias) {
        List<String> providerList = new ArrayList<String>(1);
        providerList.add (providerKey);
        return (Set<JahiaUser>) routeCallOne(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                return p.searchUsers(searchCriterias);
            }
        }, providerList, null);
    }

    /**
     * This method indicates that any internal cache for a provider should be
     * updated because the value has changed and needs to be transmitted to the
     * other nodes in a clustering environment.
     * @param jahiaUser JahiaUser the user to be updated in the cache.
     */
    public void updateCache(final JahiaUser jahiaUser) {
        routeCallOne(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                p.updateCache(jahiaUser);
                return null;
            }
        }, null, jahiaUser.getProperties());
    }

    public boolean userExists(final String name) {
        Boolean resultBool = (Boolean) routeCallAllUntilSuccess(new Command() {
            public Object execute(JahiaUserManagerProvider p) {
                if (p.userExists(name)) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }
        }, null);

        return resultBool != null && resultBool.booleanValue();
    }
    private <T> T routeCallOne(Command<T> v, List <String>providersToCall, Properties userProperties) {
        // we're calling only one of the provider, we must determine
        // which one by using the user properties matched against the
        // criteria , or by using the providersToCall parameter
        JahiaUserManagerProvider providerInstance = null;
        if (providersToCall != null && providersToCall.size () >= 1) {
            providerInstance = providersTable.get(providersToCall.get(0));
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

    private <T> T routeCallAllUntilSuccess(Command<T> v, List providersToCall) {
        // we're calling the providers in order, until one returns
        // a success condition

        Iterator providerEnum = getProvidersToCall (providersToCall).iterator();
        T result = null;
        while (providerEnum.hasNext ()) {
            JahiaUserManagerProvider curProvider =
                    (JahiaUserManagerProvider) providerEnum.next ();
            result = v.execute(curProvider);
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    private <T> List<T> routeCallAll(Command<T> v, List providersToCall) {
        // we're calling all the providers
        List<T> results = new ArrayList<T> ();

        Iterator providerEnum = getProvidersToCall (providersToCall).iterator();

        while (providerEnum.hasNext ()) {
            JahiaUserManagerProvider curProvider =
                    (JahiaUserManagerProvider) providerEnum.next ();
            results.add(v.execute(curProvider));
        }
        return results;
    }

	public boolean isUsernameSyntaxCorrect(final String name) {
		Boolean result = (Boolean) routeCallOne(new Command() {
			public Object execute(JahiaUserManagerProvider p) {
				return p.isUsernameSyntaxCorrect(name) ? Boolean.TRUE
				        : Boolean.FALSE;
			}
		}, null, null);
		return result.booleanValue();
	}

	@Override
	public void registerProvider(JahiaUserManagerProvider provider) {
		providersTable.put(provider.getKey(), provider);
		sortedProviders.add(provider);
		if (defaultProviderInstance == null || provider.isDefaultProvider()) {
			defaultProviderInstance = provider;
		}
	}
	
    public void setDefaultProvider(JahiaUserManagerProvider defaultProvider) {
    	defaultProvider.setDefaultProvider(true);
    	defaultProvider.setUserManagerService(this);
    	registerProvider(defaultProvider);
    }

// -------------------------- INNER CLASSES --------------------------

    interface Command<T> {
        T execute(JahiaUserManagerProvider p);
    }

    @Override
    public JahiaUserManagerProvider getProvider(String name) {
        return providersTable.get(name);
    }
    
}
