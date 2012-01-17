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

package org.jahia.registries;

import org.apache.commons.collections.FastHashMap;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.applications.DispatchingService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.mail.MailService;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.query.QueryService;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.SearchService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Set;

/**
 * The ServicesRegistry class that give a unique access point to Jahia Services.
 * Services are instantiated and put in an Map.<br>
 * Services are loaded from properties file.<br>
 *
 * @author Eric Vassalli
 * @author Khue Nguyen
 * @author Fulco Houkes
 */
public class ServicesRegistry {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ServicesRegistry.class);
    /**
     * It's a Singleton *
     */
    private static ServicesRegistry theObject = new ServicesRegistry();


    /**
     * Jahia Application Dispatching Service Name *
     */
    private static final String APPLICATIONS_DISPATCH_SERVICE =
            "DispatchingService";

    // Jahia Application Manager Service
    private static final String APPLICATIONS_MANAGER_SERVICE =
            "ApplicationsManagerService";

    // Jahia User Manager Service
    private static final String JAHIA_USER_MANAGER_SERVICE = "JahiaUserManagerService";
    private static final String JAHIA_GROUP_MANAGER_SERVICE =
            "JahiaGroupManagerService";

    // Jahia FileWatcher Service
    private static final String JAHIA_FILE_WATCHER_SERVICE = "JahiaFileWatcherService";

    // Jahia Multi Sites Manager Service
    private static final String JAHIA_SITES_SERVICE = "JahiaSitesService";

    // Jahia Cache factory for every cache except the HTML one
    private static final String JAHIA_CACHE_SERVICE = "JahiaCacheService";

    private static final String MAIL_SERVICE = "MailService";

    private static final String CATEGORY_SERVICE = "CategoryService";

    private static final String SCHEDULER_SERVICE = "SchedulerService";

    private static final String JCRSTORE_SERVICE = "JCRStoreService";

    private static final String JCRPUBLICATION_SERVICE = "jcrPublicationService";

    private static final String JCRVERSION_SERVICE = "jcrVersionService";

    private static final String IMPORTEXPORT_SERVICE = "ImportExportService";

    private static final String PREFERENCES_SERVICE = "JahiaPreferencesService";

    // This map is an optimization to avoid synchronization issues.
    private FastHashMap servicesCache;
    
    {
    	 servicesCache = new FastHashMap();
    	 servicesCache.setFast(true);
    }

    /**
     * Return the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static ServicesRegistry getInstance() {
        return theObject;
    }

    /**
     * Initialize the Services registry.
     *
     * @param jSettings the Jahia settings
     * @throws JahiaException when a service could not be loaded and instantiated.
     */
    public void init(SettingsBean jSettings)
            throws JahiaException {
        getServiceNames();
    }

    public void shutdown() throws JahiaException {
    	// do nothing
    }

    public Collection<? extends JahiaService> getServiceInstances() {
        return SpringContextSingleton.getInstance().getContext().getBeansOfType(JahiaService.class).values();
    }

    public Set<String> getServiceNames() {
        return SpringContextSingleton.getInstance().getContext().getBeansOfType(JahiaService.class).keySet();
    }

    // @author NK 21.12.2000
    /**
     * Retrieve the service instance associated to the service name <code>serviceName</code>.
     *
     * @param serviceName the service name
     * @return the service instance
     */
    public JahiaService getService(String serviceName) {
        JahiaService jahiaService = (JahiaService) servicesCache.get(serviceName);
        if (jahiaService != null) {
            return jahiaService;
        }
        ApplicationContext context = (ApplicationContext) SpringContextSingleton.getInstance().getContext();
        if (context != null) {
			jahiaService = (JahiaService) context.getBean(serviceName);
            servicesCache.put(serviceName, jahiaService);
		} else {
            logger.warn("Application context is not (yet) available when trying to retrieve service " + serviceName + ", will return null !");
        }
        return jahiaService;
    } // end getService


    // @author NK 21.12.2000
    public DispatchingService getApplicationsDispatchService() {
        return (DispatchingService) getService(
                APPLICATIONS_DISPATCH_SERVICE);
    }

    /**
     * Return a reference on the DB User Manager service
     */
    public JahiaUserManagerService getJahiaUserManagerService() {
        return (JahiaUserManagerService) getService(JAHIA_USER_MANAGER_SERVICE);
    }

    /**
     * Return a reference on the DB User Manager service
     */
    public JahiaGroupManagerService getJahiaGroupManagerService() {
        return (JahiaGroupManagerService) getService(JAHIA_GROUP_MANAGER_SERVICE);
    }

    /**
     * NK 12.01.2001
     */
    public JahiaFileWatcherService getJahiaFileWatcherService() {
        return (JahiaFileWatcherService) getService(JAHIA_FILE_WATCHER_SERVICE);
    }

    /**
     * NK 13.02.2001
     */
    public ApplicationsManagerService getApplicationsManagerService() {
        return (ApplicationsManagerService) getService(
                APPLICATIONS_MANAGER_SERVICE);
    }

    /**
     * NK 12.03.2001
     */
    public JahiaSitesService getJahiaSitesService() {
        return (JahiaSitesService) getService(JAHIA_SITES_SERVICE);
    }

    /**
     * Return a reference to the Jahia Cache Factory service
     */
    public CacheService getCacheService() {
        return (CacheService) getService(JAHIA_CACHE_SERVICE);
    }

    public MailService getMailService() {
        return (MailService) getService(MAIL_SERVICE);
    }

    public CategoryService getCategoryService() {
        return (CategoryService) getService(CATEGORY_SERVICE);
    }

    public SchedulerService getSchedulerService() {
        return (SchedulerService) getService(SCHEDULER_SERVICE);
    }

    public JCRStoreService getJCRStoreService() {
        return (JCRStoreService) getService(JCRSTORE_SERVICE);
    }

    public JCRPublicationService getJCRPublicationService() {
        return (JCRPublicationService) getService(JCRPUBLICATION_SERVICE);
    }

    public JCRVersionService getJCRVersionService() {
        return (JCRVersionService) getService(JCRVERSION_SERVICE);
    }

    public ImportExportService getImportExportService() {
        return (ImportExportService) getService(IMPORTEXPORT_SERVICE);
    }

    public JahiaPreferencesService getJahiaPreferencesService() {
        return (JahiaPreferencesService) getService(PREFERENCES_SERVICE);
    }

    public JahiaPasswordPolicyService getJahiaPasswordPolicyService() {
        return (JahiaPasswordPolicyService) getService("JahiaPasswordPolicyService");
    }

    public JahiaTemplateManagerService getJahiaTemplateManagerService() {
        return (JahiaTemplateManagerService) getService("JahiaTemplateManagerService");
    }

    public QueryService getQueryService() {
        return (QueryService) getService("QueryService");
    }

    public SearchService getSearchService() {
        return (SearchService) getService("SearchService");
    }

    /**
     * Default constructor, creates a new <code>ServiceRegistry</code> instance.
     */
    private ServicesRegistry() {
        super();
    }

}
