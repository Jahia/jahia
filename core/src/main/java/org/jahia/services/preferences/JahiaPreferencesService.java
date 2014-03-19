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
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
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
package org.jahia.services.preferences;


import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
import org.jahia.services.preferences.impl.JahiaPreferencesJCRProviders;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Preference service for Jahia.
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:39:09
 */
public class JahiaPreferencesService extends JahiaService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaPreferencesService.class);
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
            Class clazz = jcrStoreService.getDecorators().get(providerTypes.get(providerType));
            JahiaPreferencesJCRProviders provider;
//            try {
                Class<? extends JCRNodeWrapper> aClass = clazz.asSubclass(JCRNodeWrapper.class);
                provider = createProvider(aClass);
                providersByClass.put(aClass, provider);
//            } catch (ClassNotFoundException e) {
//                provider = new JahiaPreferencesJCRProviders();
//            }
            provider.setType(providerType);
            provider.setNodeType(providerTypes.get(providerType));
            provider.setJCRSessionFactory(jcrStoreService.getSessionFactory());
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
     * Get providers map.
     *
     * @return
     */
    private Map<String, JahiaPreferencesProvider> getProvidersMap() {
        return providers;
    }


}

