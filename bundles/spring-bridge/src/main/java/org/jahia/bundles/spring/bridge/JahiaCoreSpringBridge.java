/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
package org.jahia.bundles.spring.bridge;

import org.apache.camel.CamelContext;
import org.jahia.api.settings.SettingsBean;
import org.jahia.bin.Jahia;
import org.jahia.osgi.FrameworkService;
import org.jahia.pipelines.Pipeline;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.events.JournalEventReader;
import org.jahia.services.image.JahiaImageService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.mail.MailService;
import org.jahia.services.modulemanager.DefinitionsManagerService;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.observation.JahiaEventService;
import org.jahia.services.render.RenderService;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.SearchService;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.services.wip.WIPService;
import org.jahia.services.workflow.WorkflowService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * This class will expose some Jahia internal spring services as OSGI services.
 */
@Component(
        immediate = true,
        service = JahiaCoreSpringBridge.class,
        property = {
                org.osgi.framework.Constants.SERVICE_DESCRIPTION + "=Jahia Core Spring Bridge, will expose some Jahia " +
                        "internal spring services as OSGI services",
                Constants.SERVICE_VENDOR + "=" + Jahia.VENDOR_NAME
        }
)
public class JahiaCoreSpringBridge {

    private static final Logger logger = LoggerFactory.getLogger(JahiaCoreSpringBridge.class);

    private BundleContext bundleContext;
    private final List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    @Activate
    public void start(BundleContext bundleContext) {
        logger.info("Spring bridge starting");
        this.bundleContext = bundleContext;

        bridgeSpringService("JahiaUserManagerService", "The user manager is responsible to manage all the users in the DX environment", JahiaUserManagerService.class.getName(), org.jahia.api.usermanager.JahiaUserManagerService.class.getName());
        bridgeSpringService("JahiaGroupManagerService", "The group manager is responsible to manage all the groups in the DX environment", JahiaGroupManagerService.class.getName());
        bridgeSpringService("JahiaSitesService", "DX Multi Sites Management Service", JahiaSitesService.class.getName());
        bridgeSpringService("SchedulerService", "DX background task scheduling and management service", SchedulerService.class.getName());
        bridgeSpringService("JCRStoreService", "This is a DX service, which manages the delegation of JCR store related deployment and export functions to the right JCRStoreProvider", JCRStoreService.class.getName());
        bridgeSpringService("jcrSessionFactory", "The entry point into the content repositories provided by the JCRStoreProvider list", JCRSessionFactory.class.getName());
        bridgeSpringService("jcrTemplate", "Helper server to simplify and unify JCR data access", JCRTemplate.class.getName(), org.jahia.api.content.JCRTemplate.class.getName());
        bridgeSpringService("nodeTypeRegistry", "DX implementation of the NodeTypeManager", NodeTypeRegistry.class.getName());
        bridgeSpringService("jcrPublicationService", "This is a DX service, which offers functionality to publish, unpublish or get publication info of JCR nodes", JCRPublicationService.class.getName());
        bridgeSpringService("ComplexPublicationService", "DX complex publication service", ComplexPublicationService.class.getName());
        bridgeSpringService("ImportExportService", "DX import/export service to manipulate different types of content", ImportExportService.class.getName());
        bridgeSpringService("JahiaTemplateManagerService", "Template and template set deployment and management service", JahiaTemplateManagerService.class.getName(), org.jahia.api.templates.JahiaTemplateManagerService.class.getName());
        bridgeSpringService("org.jahia.services.templates.TemplatePackageRegistry", "Template packages registry service", TemplatePackageRegistry.class.getName());
        bridgeSpringService("SearchService", "DX search service", SearchService.class.getName());
        bridgeSpringService("JahiaFileWatcherService", "DX file watcher service", JahiaFileWatcherService.class.getName());
        bridgeSpringService("org.jahia.services.seo.jcr.VanityUrlService", "Service to manage vanity urls in DX", VanityUrlService.class.getName());
        bridgeSpringService("imageService", "This service provides access to various image manipulation operations such as image resizing, cropping and rotating", JahiaImageService.class.getName());
        bridgeSpringService("visibilityService", "Service implementation for evaluating visibility conditions on a content item", VisibilityService.class.getName());
        bridgeSpringService("ModuleManager", "Entry point interface for the module management service, " +
                "providing functionality for module deployment, undeployment, start and stop operations, which are performed " +
                "in a seamless way on a standalone installation as well as across the platform cluster", ModuleManager.class.getName());
        bridgeSpringService("DefinitionsManagerService", "Entry point interface for CND definition-related functions", DefinitionsManagerService.class.getName());
        bridgeSpringService("MailService", "DX mail service implementation", MailService.class.getName());
        bridgeSpringService("workflowService", "DX service for managing content workflow", WorkflowService.class.getName());
        bridgeSpringService("RenderService", "DX node rendering service", RenderService.class.getName());
        bridgeSpringService("journalEventReader", "DX node journal event reader", JournalEventReader.class.getName());
        bridgeSpringService("jahiaNotificationContext", "DX node camel context", CamelContext.class.getName());
        bridgeSpringService("choiceListInitializers", "DX ChoiceList initializer service", ChoiceListInitializerService.class.getName());
        bridgeSpringService("choiceListRenderers", "DX ChoiceList renderer service", ChoiceListRendererService.class.getName());
        bridgeSpringService("UrlRewriteService", "DX Url Rewrite service", UrlRewriteService.class.getName());
        bridgeSpringService("org.jahia.services.tags.TaggingService", "Tag management service", TaggingService.class.getName());
        bridgeSpringService("org.jahia.services.wip.WIPService", "Work in progress service", WIPService.class.getName());
        bridgeSpringService("settingsBean", "Settings beans", SettingsBean.class.getName());
        bridgeSpringService("ehCacheProvider", "EHCache provider", CacheProvider.class.getName());
        bridgeSpringService("jahiaEventService", "Jahia Events Service", JahiaEventService.class.getName());
        bridgeSpringService("HttpClientService", "Utility service for HTTP communication", HttpClientService.class.getName());

        Dictionary<String, Object> authPipelineProps = new Hashtable<>();
        authPipelineProps.put(Constants.SERVICE_DESCRIPTION, "Authentication pipeline");
        authPipelineProps.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        authPipelineProps.put("type", "authentication");
        bridgeSpringService("authPipeline", authPipelineProps, Pipeline.class.getName());

        Dictionary<String, Object> workflowPipelineProps = new Hashtable<>();
        workflowPipelineProps.put(Constants.SERVICE_DESCRIPTION, "Workflow people assignement pipeline");
        workflowPipelineProps.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        workflowPipelineProps.put("type", "peopleAssignmentPipeline");
        bridgeSpringService("peopleAssignmentPipeline", workflowPipelineProps, Pipeline.class.getName());

        // If JNDI is available in the OSGi framework this service will be found under 'osgi:service/jdbc/jahia'
        Dictionary<String, Object> dataSourceProps = new Hashtable<>();
        dataSourceProps.put("osgi.jndi.service.name", "jdbc/jahia");
        bridgeSpringService("dataSource", dataSourceProps, DataSource.class.getName());

        // Notify the framework that the spring bridge is started
        FrameworkService.getInstance().notifySpringBridgeStarted();
    }

    @Deactivate
    public void stop() {
        for (ServiceRegistration serviceRegistration : serviceRegistrations) {
            serviceRegistration.unregister();
        }
    }

    private void bridgeSpringService(String beanId, String description, String... serviceClass) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(Constants.SERVICE_DESCRIPTION, description);
        props.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        bridgeSpringService(beanId, props, serviceClass);
    }

    private void bridgeSpringService(String beanId, Dictionary<String, Object> props, String... serviceClass) {
        serviceRegistrations.add(bundleContext.registerService(serviceClass,
                SpringContextSingleton.getInstance().getContext().getBean(beanId),
                props));
        logger.info("Spring bean '{}' bridged to OSGI service. Interfaces: [{}]", beanId, String.join(", ", serviceClass));
    }
}
