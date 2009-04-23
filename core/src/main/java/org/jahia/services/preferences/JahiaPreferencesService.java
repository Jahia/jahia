/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.preferences;


import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.content.JCRJahiaContentNode;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.cache.CacheService;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
import org.jahia.services.preferences.page.PageJahiaPreference;
import org.jahia.services.preferences.impl.JahiaPreferencesJCRProviders;
import org.jahia.registries.ServicesRegistry;

import javax.jcr.RepositoryException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:39:09
 */
public class JahiaPreferencesService extends JahiaService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesService.class);
    private static JahiaPreferencesService instance;
    private CacheService cacheService;
    private JCRStoreService jcrStoreService;

    private Map<String, String> providerTypes;
    private Map<String, JahiaPreferencesProvider> providers;
    private Map<Class, JahiaPreferencesProvider> providersByClass;

    public synchronized void start() throws JahiaInitializationException {
        logger.debug("** Initializing the Preferences Service ...");
        providers = new HashMap<String, JahiaPreferencesProvider>();
        providersByClass = new HashMap<Class, JahiaPreferencesProvider>();
        for (String providerType : providerTypes.keySet()) {
            String className = jcrStoreService.getDecorators().get(providerTypes.get(providerType));
            JahiaPreferencesJCRProviders provider;
            try {
                Class<? extends JCRNodeWrapper> aClass = Class.forName(className).asSubclass(JCRNodeWrapper.class);
                provider = createProvider(aClass);
                providersByClass.put(aClass, provider);
            } catch (ClassNotFoundException e) {
                provider = new JahiaPreferencesJCRProviders();
            }
            provider.setType(providerType);
            provider.setNodeType(providerTypes.get(providerType));
            provider.setJCRStoreService(jcrStoreService);
            providers.put(providerType, provider);
        }
    }

    public <T extends JCRNodeWrapper> JahiaPreferencesJCRProviders<T> createProvider(Class<T> c) {
        return new JahiaPreferencesJCRProviders<T>();
    }

    public synchronized void stop() {
        logger.debug("** Stop the Preferences Service ...");
    }

    public static synchronized JahiaPreferencesService getInstance() {
        if (instance == null) {
            instance = new JahiaPreferencesService();
        }
        return instance;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public JCRStoreService getJcrStoreService() {
        return jcrStoreService;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public Map<String, String> getProviderTypes() {
        return providerTypes;
    }

    public void setProviderTypes(Map<String, String> providerTypes) {
        this.providerTypes = providerTypes;
    }

    public Map getProviders() {
        return providers;
    }

    public void setProviders(Map providers) {
        this.providers = providers;
    }

    /**
     * Delete all preferences of the current user
     *
     * @param processingContext
     */
    public void deleteCurrentUserPreferences(ProcessingContext processingContext) {
        deleteAllPreferencesByPrincipal(processingContext.getUser());
    }

    /**
     * Delete principal's preferences
     *
     * @param principal
     */
    public void deleteAllPreferencesByPrincipal(Principal principal) {
        Map<String, JahiaPreferencesProvider> allProviders = getProvidersMap();
        Iterator providersIt = allProviders.values().iterator();
        while (providersIt.hasNext()) {
            JahiaPreferencesProvider jahiaPreferencesProvider = (JahiaPreferencesProvider) providersIt.next();
            jahiaPreferencesProvider.deleteAllPreferencesByPrincipal(principal);
        }
    }


    /**
     * Get provider by type
     *
     * @param providerType
     * @return
     * @throws JahiaPreferenceProviderException
     *
     */
    public JahiaPreferencesProvider getPreferencesProviderByType(String providerType) throws JahiaPreferenceProviderException {
        return providers.get(providerType);
    }

    public <T extends JCRNodeWrapper> JahiaPreferencesProvider<T> getPreferencesProviderByClass(Class<T> c) throws JahiaPreferenceProviderException {
        return providersByClass.get(c);
    }


    /**
     * Get Generic preference provider
     *
     * @return
     * @throws JahiaPreferenceProviderException
     *
     */
    public JahiaPreferencesProvider<GenericJahiaPreference> getGenericPreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("simple");
    }

    /**
     * Get Page preference provider
     *
     * @return
     * @throws JahiaPreferenceProviderException
     *
     */
    public JahiaPreferencesProvider<PageJahiaPreference> getPagePreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("page");
    }

    /**
     * Get providers map.
     *
     * @return
     */
    private Map<String, JahiaPreferencesProvider> getProvidersMap() {
        return providers;
    }

    /**
     * Return the value associated with the given key using the generic preference provider.
     *
     * @param key     the key
     * @param jParams the processing context
     * @return the value
     */
    public String getGenericPreferenceValue(String key, ProcessingContext jParams) {
        try {

            JahiaPreference preference = getGenericPreferencesProvider().getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getSimpleXpath(key));
            if (preference != null) {
                try {
                    return ((GenericJahiaPreference) preference.getNode()).getPrefValue();
                } catch (RepositoryException e) {
                    logger.error("Preference provider was not found.", e);
                }
            }
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
        return null;
    }

    /**
     * Return the value associated with the given key using the generic preference provider.
     *
     * @param key          the key
     * @param defaultValue the default value to be returned in te preference was not found
     * @param jParams      the processing context
     * @return the value
     */
    public boolean getGenericPreferenceBooleanValue(String key, boolean defaultValue, ProcessingContext jParams) {
        String value = getGenericPreferenceValue(key, jParams);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Return the value associated with the given key using the page preference provider.
     *
     * @param key     the key
     * @param jParams the processing context
     * @return the value
     */
    public String getPagePreferenceValue(String key, ProcessingContext jParams) {
        try {
            JahiaPreference<PageJahiaPreference> preference = getPagePreferencesProvider().getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getPageXpath(jParams.getPageID(), key));
            if (preference != null) {
                return preference.getNode().getPrefValue();
            }
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
        return null;
    }


    /**
     * Set a preference value associated with the given key using the generic preference provider.
     *
     * @param prefName  the key
     * @param prefValue the value
     * @param jParams   the processing context
     */
    public void setGenericPreferenceValue(String prefName, String prefValue, ProcessingContext jParams) {
        try {
            JahiaPreferencesProvider<GenericJahiaPreference> basicProvider = getGenericPreferencesProvider();

            // create generic preference key
            JahiaPreference<GenericJahiaPreference> preference = basicProvider.getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getSimpleXpath(prefName));
            if (preference == null) {
                if(prefValue == null){
                    return;
                }

                preference = basicProvider.createJahiaPreferenceNode(jParams);
                preference.getNode().setPrefName(prefName);
            }else{
                // delete preference
                if(prefValue == null){
                    basicProvider.deleteJahiaPreference(preference);
                    return;
                }
            }

            // create genereic preference value
            preference.getNode().setPrefValue(prefValue);


            basicProvider.setJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * Set a preference value associated with the given key using the page preference provider.
     *
     * @param prefName  the key
     * @param prefValue the value
     * @param jParams   the processing context
     */
    public void setPagePreferenceValue(String prefName, String prefValue, ProcessingContext jParams) {
        try {
            JahiaPreferencesProvider<PageJahiaPreference> pageProvider = getPagePreferencesProvider();

            // create generic preference key
            JahiaPreference<PageJahiaPreference> preference = pageProvider.getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getPageXpath(jParams.getPageID(), prefName));
            if (preference == null) {
                preference = pageProvider.createJahiaPreferenceNode(jParams);
                PageJahiaPreference node = preference.getNode();
                node.setPrefName(prefName);
                ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jParams.getPageID(), false);
                String pageUUID = page.getUUID();
                node.setPageUUID(pageUUID);
            }

            // create genereic preference value
            preference.getNode().setPrefValue(prefValue);

            pageProvider.setJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * @param prefName the key
     * @param jParams  the processing context
     */
    public void deleteGenericPreferenceValue(String prefName, ProcessingContext jParams) {
        try {
            // create generic preference key
            JahiaPreference<GenericJahiaPreference> preference = getGenericPreferencesProvider().createJahiaPreferenceNode(jParams);
            preference.getNode().setPrefName(prefName);

            getGenericPreferencesProvider().deleteJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * @param prefName the key
     * @param jParams  the processing context
     */
    public void deletePagePreferenceValue(String prefName, ProcessingContext jParams) {
        try {
            // create generic preference key
            JahiaPreference<PageJahiaPreference> preference = getPagePreferencesProvider().createJahiaPreferenceNode(jParams);
            ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jParams.getPageID(), false);
            String pageUUID = page.getUUID();
            preference.getNode().setPageUUID(pageUUID);
            preference.getNode().setPrefName(prefName);

            getPagePreferencesProvider().deleteJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

}

