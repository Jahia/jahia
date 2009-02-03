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

package org.jahia.registries;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.mbeans.JahiaMBeanServer;
import org.jahia.query.QueryService;
import org.jahia.resourcebundle.ResourceBundleService;
import org.jahia.services.JahiaService;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.opensearch.JahiaOpenSearchService;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.applications.DispatchingService;
import org.jahia.services.audit.JahiaAuditLogManagerService;
import org.jahia.services.cache.CacheKeyGeneratorService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.events.JahiaEventGeneratorService;
import org.jahia.services.fetchers.JahiaFetcherService;
import org.jahia.services.fields.JahiaFieldService;
import org.jahia.services.files.JahiaTextFileService;
import org.jahia.services.htmleditors.HtmlEditorsService;
import org.jahia.services.htmlparser.HtmlParserService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockService;
import org.jahia.services.mail.MailService;
import org.jahia.services.metadata.MetadataService;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.JahiaPageTemplateService;
import org.jahia.services.pagesusers.JahiaPageUserPropService;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.indexingscheduler.JahiaSearchIndexationService;
import org.jahia.services.shares.AppsShareService;
import org.jahia.services.sitemap.JahiaSiteMapService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sso.CasService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates_deployer.JahiaTemplatesDeployerService;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.toolbar.JahiaToolbarService;
import org.jahia.services.urlrewriting.URLRewritingService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteUserManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.JahiaVersionService;
import org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.settings.SettingsBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.*;

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

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ServicesRegistry.class);

    /**
     * It's a Singleton *
     */
    private static ServicesRegistry theObject = new ServicesRegistry();


    // Jahia Files Services
    private static final String TEXT_FILE_SERVICE = "JahiaTextFileService";

    /**
     * Jahia Page Services Name *
     */
    private static final String JAHIA_PAGE_SERVICE = "JahiaPageService";

    /**
     * Jahia Page Template Service name
     */
    private static final String JAHIA_PAGE_TEMPLATE_SERVICE =
            "JahiaPageTemplateService";

    /**
     * Jahia Fields Service Name *
     */
    private static final String JAHIA_FIELD_SERVICE = "JahiaFieldService";

    /**
     * Jahia Application Dispatching Service Name *
     */
    private static final String APPLICATIONS_DISPATCH_SERVICE =
            "DispatchingService";

    // Jahia Application Manager Service
    private static final String APPLICATIONS_MANAGER_SERVICE =
            "ApplicationsManagerService";

    // Jahia Containers Services
    private static final String JAHIA_CONTAINERS_SERVICE = "JahiaContainersService";

    // Jahia User Manager Service
    private static final String JAHIA_USER_MANAGER_SERVICE = "JahiaUserManagerService";
    private static final String JAHIA_GROUP_MANAGER_SERVICE =
            "JahiaGroupManagerService";
    private static final String JAHIA_SITE_USER_MANAGER_SERVICE =
            "JahiaSiteUserManagerService";
    private static final String JAHIA_SITE_GROUP_MANAGER_SERVICE =
            "JahiaSiteGroupManagerService";

    // Jahia ACL Manager Service
    private static final String JAHIA_ACL_MANAGER_SERVICE = "JahiaACLManagerService";

    // Jahia SearchManager
    private static final String JAHIA_SEARCH_SERVICE = "JahiaSearchService";

    // Jahia SearchIndexation Service
    private static final String JAHIA_SEARCH_INDEXATION_SERVICE = "JahiaSearchIndexationService";

    // Jahia Fetcher Service
    private static final String JAHIA_FETCHER_SERVICE = "JahiaFetcherService";

    // Jahia Templates Deployer Service
    private static final String JAHIA_TEMPLATES_DEPLOYER_SERVICE =
            "JahiaTemplatesDeployerService";

    // Jahia WebApps Deployer Service
    private static final String JAHIA_WEBAPPS_DEPLOYER_SERVICE =
            "JahiaWebAppsDeployerService";

    // Jahia FileWatcher Service
    private static final String JAHIA_FILE_WATCHER_SERVICE = "JahiaFileWatcherService";

    // Jahia Event Service
    private static final String JAHIA_EVENT_SERVICE = "JahiaEventService";

    // Jahia Audit Log Manager Service
    private static final String JAHIA_AUDIT_LOG_MANAGER_SERVICE =
            "JahiaAuditLogManagerService";

    // Jahia Multi Sites Manager Service
    private static final String JAHIA_SITES_SERVICE = "JahiaSitesService";

    // Jahia Workflow service
    private static final String WORKFLOW_SERVICE = "WorkflowService";

    // Jahia Applications Share Service
    private static final String JAHIA_APPS_SHARE_SERVICE = "AppsShareService";

    // Jahia Versioning Service
    private static final String JAHIA_VERSION_SERVICE = "JahiaVersionService";

    // Jahia Html Editors Service
    private static final String JAHIA_HTMLEDITORS_SERVICE = "JahiaHtmlEditorsService";

    // Jahia ResourceBundles Service
    private static final String JAHIA_RESOURCEBUNDLE_SERVICE =
            "JahiaResourceBundleService";

    // Jahia Cache factory for every cache except the HTML one
    private static final String JAHIA_CACHE_SERVICE = "JahiaCacheService";

    //Jahia site map helper
    private static final String JAHIA_SITE_MAP_SERVICE = "JahiaSiteMapService";

    private static final String MAIL_SERVICE = "MailService";

    private static final String LOCK_SERVICE = "LockService";

    private static final String CATEGORY_SERVICE = "CategoryService";

    private static final String HTML_PARSER_SERVICE = "HtmlParserService";

    private static final String URL_REWRITING_SERVICE = "URLRewritingService";

    private static final String METADATA_SERVICE = "MetadataService";

    private static final String SCHEDULER_SERVICE = "SchedulerService";

    private static final String JCRSTORE_SERVICE = "JCRStoreService";

    private static final String IMPORTEXPORT_SERVICE = "ImportExportService";

    private static final String PAGEUSERPROP_SERVICE = "JahiaPageUserPropService";

    private static final String PREFERENCES_SERVICE = "JahiaPreferencesService";

    // BEGIN [added by Pascal Aubry for CAS authentication]
    private static final String CAS_SERVICE = "CasService";
    // END [added by Pascal Aubry for CAS authentication]

    private static final String CLUSTER_SERVICE = "clusterService";

    private static final String TIME_BASED_PUBLISHING_SERVICE = "TimeBasedPublishingService";

    private static final String JAHIA_TOOLBAR_SERVICE = "JahiaToolbarService";

    private static final String JAHIA_OPENSEARCH_SERVICE = "JahiaOpenSearchService";

    // This map is an optimization to avoid synchronization issues.
    private Map<String, JahiaService> servicesCache = new HashMap<String, JahiaService>();

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
     * @throws JahiaException when a service could not be loaded and instanciated.
     */
    public void init(SettingsBean jSettings)
            throws JahiaException {
        getServiceNames();

//        while (serviceNameIter.hasNext()) {
//            String curServiceName = (String) serviceNameIter.next();
//            initService(curServiceName);
//        }
    }

    public void shutdown() throws JahiaException {
//        while (serviceNameIter.hasNext()) {
//            String curServiceName = (String) serviceNameIter.next();
//            shutdown(curServiceName);
//        }
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
        JahiaService jahiaService = servicesCache.get(serviceName);
        if (jahiaService != null) {
            return jahiaService;
        }
        ApplicationContext context = (ApplicationContext) SpringContextSingleton.getInstance().getContext();
        if (context != null) {
			jahiaService = (JahiaService) context.getBean(serviceName);
        servicesCache.put(serviceName, jahiaService);
		}
        return jahiaService;
    } // end getService


    // @author NK 21.12.2000
    /**
     * method getJahiaFileTransFerService
     */
    public JahiaTextFileService getJahiaTextFileService() {
        return (JahiaTextFileService) getService(TEXT_FILE_SERVICE);
    }

    // @author NK 21.12.2000
    /**
     * method getJahiaPageService
     */
    public JahiaPageService getJahiaPageService() {
        return (JahiaPageService) getService(JAHIA_PAGE_SERVICE);
    }

    // @author NK 21.12.2000
    /**
     * method getJahiaPageTemplateService
     */
    public JahiaPageTemplateService getJahiaPageTemplateService() {
        return (JahiaPageTemplateService) getService(JAHIA_PAGE_TEMPLATE_SERVICE);
    }

    // @author NK 21.12.2000
    /**
     * method getJahiaFieldService
     */
    public JahiaFieldService getJahiaFieldService() {
        return (JahiaFieldService) getService(JAHIA_FIELD_SERVICE);
    }

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
     *
     */
    public JahiaACLManagerService getJahiaACLManagerService() {
        return (JahiaACLManagerService) getService(JAHIA_ACL_MANAGER_SERVICE);
    }

    /**
     *
     */
    public JahiaContainersService getJahiaContainersService() {
        return (JahiaContainersService) getService(JAHIA_CONTAINERS_SERVICE);
    }

    /**
     * DJ 03.01.2001
     */
    public JahiaSearchService getJahiaSearchService() {
        return (JahiaSearchService) getService(JAHIA_SEARCH_SERVICE);
    }

    /**
     * DJ 03.01.2001
     */
    public JahiaSearchIndexationService getJahiaSearchIndexationService() {
        return (JahiaSearchIndexationService) getService(JAHIA_SEARCH_INDEXATION_SERVICE);
    }

    /**
     * EV 11.01.2001
     */
    public JahiaFetcherService getJahiaFetcherService() {
        return (JahiaFetcherService) getService(JAHIA_FETCHER_SERVICE);
    }

    /**
     * NK 20.01.2001
     */
    public JahiaTemplatesDeployerService getJahiaTemplatesDeployerService() {
        return (JahiaTemplatesDeployerService) getService(JAHIA_TEMPLATES_DEPLOYER_SERVICE);
    }

    /**
     * NK 24.01.2001
     */
    public JahiaWebAppsDeployerService getJahiaWebAppsDeployerService() {
        return (JahiaWebAppsDeployerService) getService(JAHIA_WEBAPPS_DEPLOYER_SERVICE);
    }

    /**
     * NK 12.01.2001
     */
    public JahiaFileWatcherService getJahiaFileWatcherService() {
        return (JahiaFileWatcherService) getService(JAHIA_FILE_WATCHER_SERVICE);
    }

    /**
     * EV 12.01.2001
     */
    public JahiaEventGeneratorService getJahiaEventService() {
        return (JahiaEventGeneratorService) getService(JAHIA_EVENT_SERVICE);
    }

    /**
     * NK 13.02.2001
     */
    public ApplicationsManagerService getApplicationsManagerService() {
        return (ApplicationsManagerService) getService(
                APPLICATIONS_MANAGER_SERVICE);
    }

    /**
     * MJ 20.02.2001
     */
    public JahiaAuditLogManagerService getJahiaAuditLogManagerService() {
        return (JahiaAuditLogManagerService) getService(JAHIA_AUDIT_LOG_MANAGER_SERVICE);
    }

    /**
     * NK 12.03.2001
     */
    public JahiaSitesService getJahiaSitesService() {
        return (JahiaSitesService) getService(JAHIA_SITES_SERVICE);
    }

    /**
     * Return a reference on the DB Site User Manager service
     */
    public JahiaSiteUserManagerService getJahiaSiteUserManagerService() {
        return (JahiaSiteUserManagerService) getService(JAHIA_SITE_USER_MANAGER_SERVICE);
    }

    /**
     * Return a reference on the DB Site Group Manager service
     */
    public JahiaSiteGroupManagerService getJahiaSiteGroupManagerService() {
        return (JahiaSiteGroupManagerService) getService(JAHIA_SITE_GROUP_MANAGER_SERVICE);
    }

    /**
     * Return a reference on the apps share service
     */
    public AppsShareService getAppsShareService() {
        return (AppsShareService) getService(JAHIA_APPS_SHARE_SERVICE);
    }

    /**
     * Return a reference to the version service
     */
    public JahiaVersionService getJahiaVersionService() {
        return (JahiaVersionService) getService(JAHIA_VERSION_SERVICE);
    }

    /**
     * Return a reference to the Jahia Cache Factory service
     */
    public CacheService getCacheService() {
        return (CacheService) getService(JAHIA_CACHE_SERVICE);
    }

    public JahiaSiteMapService getJahiaSiteMapService() {
        return (JahiaSiteMapService) getService(JAHIA_SITE_MAP_SERVICE);
    }

    public HtmlEditorsService getHtmlEditorsService() {
        return (HtmlEditorsService) getService(JAHIA_HTMLEDITORS_SERVICE);
    }

    public ResourceBundleService getResourceBundleService() {
        return (ResourceBundleService) getService(
                JAHIA_RESOURCEBUNDLE_SERVICE);
    }

    public MailService getMailService() {
        return (MailService) getService(MAIL_SERVICE);
    }

    public LockService getLockService() {
        return (LockService) getService(LOCK_SERVICE);
    }
    public CategoryService getCategoryService() {
        return (CategoryService) getService(CATEGORY_SERVICE);
    }

    public HtmlParserService getHtmlParserService() {
        return (HtmlParserService) getService(HTML_PARSER_SERVICE);
    }

    public URLRewritingService getURLRewritingService() {
        return (URLRewritingService) getService(URL_REWRITING_SERVICE);
    }

    public WorkflowService getWorkflowService() {
        return (WorkflowService) getService(WORKFLOW_SERVICE);
    }

    public MetadataService getMetadataService() {
        return (MetadataService) getService(METADATA_SERVICE);
    }

    public SchedulerService getSchedulerService() {
        return (SchedulerService) getService(SCHEDULER_SERVICE);
    }

    public JCRStoreService getJCRStoreService() {
        return (JCRStoreService) getService(JCRSTORE_SERVICE);
    }

    // BEGIN [added by Pascal Aubry for CAS authentication]
    /**
     * Return a reference to the CAS service.
     */
    public CasService getCasService() {
        return (CasService) getService(CAS_SERVICE);
    }
    // END [added by Pascal Aubry for CAS authentication]

    public ClusterService getClusterService() {
        return (ClusterService) getService(CLUSTER_SERVICE);
    }

    public TimeBasedPublishingService getTimeBasedPublishingService() {
        return (TimeBasedPublishingService) getService(TIME_BASED_PUBLISHING_SERVICE);
    }

    public ImportExportService getImportExportService() {
        return (ImportExportService) getService(IMPORTEXPORT_SERVICE);
    }

    public JahiaPageUserPropService getJahiaPageUserPropService() {
        return (JahiaPageUserPropService) getService(PAGEUSERPROP_SERVICE);
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

    public CacheKeyGeneratorService getCacheKeyGeneratorService() {
        return (CacheKeyGeneratorService) getService("CacheKeyGeneratorService");
    }

     public JahiaToolbarService getJahiaToolbarService() {
        return (JahiaToolbarService) getService(JAHIA_TOOLBAR_SERVICE);
    }

    public JahiaOpenSearchService getOpenSearchService() {
       return (JahiaOpenSearchService) getService(JAHIA_OPENSEARCH_SERVICE);
   }

    public SubscriptionService getSubscriptionService() {
        return (SubscriptionService) getService("SubscriptionService");
    }

    /**
     * Default constructor, creates a new <code>ServiceRegistry</code> instance.
     */
    private ServicesRegistry() {
    }

}
