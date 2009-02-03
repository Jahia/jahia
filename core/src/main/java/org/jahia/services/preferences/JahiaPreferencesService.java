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
import org.jahia.services.cache.CacheService;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.exception.JahiaPreferencesVersionningException;
import org.jahia.services.preferences.generic.GenericJahiaPreferenceKey;
import org.jahia.services.preferences.generic.GenericJahiaPreferenceValue;
import org.jahia.services.preferences.page.PageJahiaPreferenceKey;
import org.jahia.services.preferences.page.PageJahiaPreferenceValue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.BeansException;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:39:09
 */
public class JahiaPreferencesService extends JahiaService implements ApplicationContextAware {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesService.class);
    private static JahiaPreferencesService instance;
    private CacheService cacheService;
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public synchronized void start() throws JahiaInitializationException {
        logger.debug("** Initializing the Preferences Service ...");
        debug();

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

    private void debug() {
        if (logger.isDebugEnabled()) {
            Map allProviders = getProvidersMap();
            Iterator providersIt = allProviders.values().iterator();
            while (providersIt.hasNext()) {
                JahiaPreferencesProvider jahiaPreferencesProvider = (JahiaPreferencesProvider) providersIt.next();
                logger.debug("jahiaPreferencesProvider:" + jahiaPreferencesProvider.getType());
            }
        }
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
        Map allProviders = getProvidersMap();
        Iterator providersIt = allProviders.values().iterator();
        while (providersIt.hasNext()) {
            JahiaPreferencesProvider jahiaPreferencesProvider = (JahiaPreferencesProvider) providersIt.next();
            jahiaPreferencesProvider.deleteAllPreferencesByPrincipal(principal);
        }
    }

    /**
     * Restore all prefences
     *
     * @param date
     * @throws JahiaPreferencesVersionningException
     *
     */
    public void restore(Date date) throws JahiaPreferencesVersionningException {
        Map allProviders = getProvidersMap();
        Iterator providersIt = allProviders.keySet().iterator();
        while (providersIt.hasNext()) {
            JahiaPreferencesProvider jahiaPreferencesProvider = (JahiaPreferencesProvider) providersIt.next();
            jahiaPreferencesProvider.restore(date);
        }
    }

    /**
     * Save all preferences (versionning)
     *
     * @param date
     * @throws JahiaPreferencesVersionningException
     *
     */
    public void save(Date date) throws JahiaPreferencesVersionningException {
        Map allProviders = getProvidersMap();
        Iterator providersIt = allProviders.keySet().iterator();
        while (providersIt.hasNext()) {
            JahiaPreferencesProvider jahiaPreferencesProvider = (JahiaPreferencesProvider) providersIt.next();
            jahiaPreferencesProvider.save(date);
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
        Object o = applicationContext.getBean(providerType);
        if (o == null) {
            throw new JahiaPreferenceProviderException();
        } else if (o instanceof JahiaPreferencesProvider) {
            return (JahiaPreferencesProvider) o;
        } else {
            throw new JahiaPreferenceProviderException();
        }
    }

    /**
     * Get Generic preference provider
     *
     * @return
     * @throws JahiaPreferenceProviderException
     *
     */
    public JahiaPreferencesProvider getGenericPreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("org.jahia.preferences.provider.generic");
    }

    /**
     * Get Page preference provider
     *
     * @return
     * @throws JahiaPreferenceProviderException
     *
     */
    public JahiaPreferencesProvider getPagePreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("org.jahia.preferences.provider.page");
    }

    /**
     * Get all providers
     *
     * @return
     */
    public Iterator getAllProviders() {
        Map allProviders = getProvidersMap();
        return allProviders.keySet().iterator();
    }

    /**
     * Get providers map.
     *
     * @return
     */
    private Map getProvidersMap() {
        Map providersMap = applicationContext.getBeansOfType(JahiaPreferencesProvider.class);
        if (providersMap == null) {
            logger.warn("There is no preference provider set.");
            return new HashMap();
        }
        return providersMap;
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
            // create generic preference key
            GenericJahiaPreferenceKey preferenceKey = (GenericJahiaPreferenceKey) getGenericPreferencesProvider().createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setName(key);

            JahiaPreference value = getGenericPreferencesProvider().getJahiaPreference(preferenceKey);
            if (!value.isEmpty()) {
                GenericJahiaPreferenceValue val = (GenericJahiaPreferenceValue) value.getValue();
                return val.getValue();
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
            // create page preference key
            PageJahiaPreferenceKey preferenceKey = (PageJahiaPreferenceKey) getPagePreferencesProvider().createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setPid(jParams.getPageID());
            preferenceKey.setName(key);

            JahiaPreference value = getPagePreferencesProvider().getJahiaPreference(preferenceKey);
            if (!value.isEmpty()) {
                PageJahiaPreferenceValue val = (PageJahiaPreferenceValue) value.getValue();
                return val.getValue();
            }
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
        return null;
    }

    public String getPagePreferenceValue(Principal p, String key, ProcessingContext jParams) {
        try {
            // create page preference key
            PageJahiaPreferenceKey preferenceKey = (PageJahiaPreferenceKey) getPagePreferencesProvider().createPartialJahiaPreferenceKey(p);
            preferenceKey.setPid(jParams.getPageID());
            preferenceKey.setName(key);

            JahiaPreference value = getPagePreferencesProvider().getJahiaPreference(preferenceKey);
            if (!value.isEmpty()) {
                PageJahiaPreferenceValue val = (PageJahiaPreferenceValue) value.getValue();
                return val.getValue();
            }
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
        return null;
    }

    /**
     * Return the value associated with the given wokflowState and key using the page preference provider.
     *
     * @param key     the key
     * @param jParams the processing context
     * @return the value
     */
    /* public String getStagedPagePreferenceValue(int workflowState, String key, ProcessingContext jParams) {
       try {
           // create page preference key
           PageJahiaPreferenceStagingKey preferenceKey = (PageJahiaPreferenceStagingKey) getPagePreferencesProvider().createPartialJahiaPreferenceKey(jParams);
           preferenceKey.setPid(jParams.getPageID());
           preferenceKey.setName(key);
           preferenceKey.setWorkflowState(workflowState);

           JahiaPreference value = getPagePreferencesProvider().getJahiaPreference(preferenceKey);
           if (!value.isEmpty()) {
               PageJahiaPreferenceValue val = (PageJahiaPreferenceValue) value.getValue();
               return val.getValue();
           }
       } catch (JahiaPreferenceProviderException e) {
           logger.error("Preference provider was not found.", e);
       }
       return null;
   } */

    /**
     * Set a preference value associated with the given key using the generic preference provider.
     *
     * @param key     the key
     * @param value   the value
     * @param jParams the processing context
     */
    public void setGenericPreferenceValue(String key, String value, ProcessingContext jParams) {
        try {
            JahiaPreferencesProvider basicProvider = getGenericPreferencesProvider();

            // create generic preference key
            GenericJahiaPreferenceKey preferenceKey = (GenericJahiaPreferenceKey) basicProvider.createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setName(key);

            // create genereic preference value
            GenericJahiaPreferenceValue preferenceValue = (GenericJahiaPreferenceValue) basicProvider.createEmptyJahiaPreferenceValue();
            preferenceValue.setValue(value);

            basicProvider.setJahiaPreference(preferenceKey, preferenceValue);
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * Set a preference value associated with the given key using the page preference provider.
     *
     * @param key     the key
     * @param value   the value
     * @param jParams the processing context
     */
    public void setPagePreferenceValue(String key, String value, ProcessingContext jParams) {
        try {
            JahiaPreferencesProvider pageProvider = getPagePreferencesProvider();

            // create generic preference key
            PageJahiaPreferenceKey preferenceKey = (PageJahiaPreferenceKey) pageProvider.createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setPid(jParams.getPageID());
            preferenceKey.setName(key);

            // create genereic preference value
            PageJahiaPreferenceValue preferenceValue = (PageJahiaPreferenceValue) pageProvider.createEmptyJahiaPreferenceValue();
            preferenceValue.setValue(value);

            pageProvider.setJahiaPreference(preferenceKey, preferenceValue);
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * Set a preference value associated with the given key using the page preference provider.
     *
     * @param key     the key
     * @param value   the value
     * @param jParams the processing context
     */
    /* public void setPagePreferenceStagedValue(int workflowSate, String key, String value, ProcessingContext jParams) {
        try {
            JahiaPreferencesProvider pageProvider = getPagePreferencesProvider();

            // create generic preference key
            PageJahiaPreferenceStagingKey preferenceKey = (PageJahiaPreferenceStagingKey) pageProvider.createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setPid(jParams.getPageID());
            preferenceKey.setName(key);
            preferenceKey.setWorkflowState(workflowSate);

            // create genereic preference value
            PageJahiaPreferenceValue preferenceValue = (PageJahiaPreferenceValue) pageProvider.createEmptyJahiaPreferenceValue();
            preferenceValue.setValue(value);

            pageProvider.setJahiaPreference(preferenceKey, preferenceValue);
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
    }*/

    /**
     * @param key     the key
     * @param jParams the processing context
     */
    public void deleteGenericPreferenceValue(String key, ProcessingContext jParams) {
        try {
            // create generic preference key
            GenericJahiaPreferenceKey preferenceKey = (GenericJahiaPreferenceKey) getGenericPreferencesProvider().createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setName(key);

            getGenericPreferencesProvider().deleteJahiaPreference(preferenceKey);
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * @param key     the key
     * @param jParams the processing context
     */
    public void deletePagePreferenceValue(String key, ProcessingContext jParams) {
        try {
            // create generic preference key
            PageJahiaPreferenceKey preferenceKey = (PageJahiaPreferenceKey) getPagePreferencesProvider().createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setPid(jParams.getPageID());
            preferenceKey.setName(key);

            getPagePreferencesProvider().deleteJahiaPreference(preferenceKey);
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * @param key     the key
     * @param jParams the processing context
     */
    /*  public void deleteStagedPagePreferenceValue(int workflowState, String key, ProcessingContext jParams) {
        try {
            // create generic preference key
            PageJahiaPreferenceStagingKey preferenceKey = (PageJahiaPreferenceStagingKey) getPagePreferencesProvider().createPartialJahiaPreferenceKey(jParams);
            preferenceKey.setPid(jParams.getPageID());
            preferenceKey.setWorkflowState(workflowState);
            preferenceKey.setName(key);

            getPagePreferencesProvider().deleteJahiaPreference(preferenceKey);
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
    }*/


}

