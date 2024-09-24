/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extension.registry;

import org.jahia.ajax.gwt.helper.ModuleGWTResources;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.bin.listeners.HttpListener;
import org.jahia.bin.listeners.HttpListenersRegistry;
import org.jahia.params.valves.LoginConfig;
import org.jahia.params.valves.LoginUrlProvider;
import org.jahia.params.valves.LogoutConfig;
import org.jahia.params.valves.LogoutUrlProvider;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ProviderFactory;
import org.jahia.services.content.decorator.JCRNodeDecoratorDefinition;
import org.jahia.services.content.decorator.validation.JCRNodeValidatorDefinition;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.content.nodetypes.renderer.ModuleChoiceListRenderer;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.content.rules.ModuleGlobalObject;
import org.jahia.services.observation.JahiaEventListener;
import org.jahia.services.observation.JahiaEventServiceImpl;
import org.jahia.services.pwd.PasswordDigester;
import org.jahia.services.render.StaticAssetMapping;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.cache.CacheKeyPartGenerator;
import org.jahia.services.search.SearchProvider;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.services.visibility.VisibilityConditionRule;
import org.jahia.services.workflow.WorklowTypeRegistration;
import org.osgi.service.component.annotations.*;
import org.springframework.web.servlet.HandlerMapping;

/**
 * OSGI registry for Jahia services.
 * This registry allows to register following Jahia extensions as OSGI services:
 *
 * RenderFilter, ErrorHandler, Action, ModuleChoiceListInitializer, ModuleChoiceListRenderer,
 * ModuleGlobalObject, StaticAssetMapping, DefaultEventListener, BackgroundAction, WorklowTypeRegistration,
 * VisibilityConditionRule, JCRNodeDecoratorDefinition, JCRNodeValidatorDefinition, CacheKeyPartGenerator,
 * HandlerMapping, ProviderFactory, AbstractServletFilter, SearchProvider, ModuleGWTResources, PasswordDigester,
 * LoginUrlProvider, LogoutUrlProvider, HttpListener, JahiaEventListener.
 */
@Component(service = OSGIRegistry.class, immediate = true)
public class OSGIRegistry {

    private void registerOSGIService(Object service) {
        ((TemplatePackageRegistry.JahiaExtensionsRegistry) SpringContextSingleton.getBean("JahiaExtensionsRegistry"))
                .register(service, "osgi service " + service.getClass().getName());
    }

    private void unregisterOSGIService(Object service) {
        ((TemplatePackageRegistry.JahiaExtensionsRegistry) SpringContextSingleton.getBean("JahiaExtensionsRegistry"))
                .unregister(service, "osgi service " + service.getClass().getName());
    }

    @Reference(service = RenderFilter.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerRenderFilter(RenderFilter registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = ErrorHandler.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerErrorHandler(ErrorHandler registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = Action.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerAction(Action registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = ModuleChoiceListInitializer.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerModuleChoiceListInitializer(ModuleChoiceListInitializer registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = ModuleChoiceListRenderer.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerModuleChoiceListRenderer(ModuleChoiceListRenderer registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = ModuleGlobalObject.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerModuleGlobalObject(ModuleGlobalObject registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = StaticAssetMapping.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerStaticAssetMapping(StaticAssetMapping registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = DefaultEventListener.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerDefaultEventListener(DefaultEventListener registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = BackgroundAction.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerBackgroundAction(BackgroundAction registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = WorklowTypeRegistration.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerWorklowTypeRegistration(WorklowTypeRegistration registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = VisibilityConditionRule.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerVisibilityConditionRule(VisibilityConditionRule registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = JCRNodeDecoratorDefinition.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerJCRNodeDecoratorDefinition(JCRNodeDecoratorDefinition registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = JCRNodeValidatorDefinition.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerJCRNodeValidatorDefinition(JCRNodeValidatorDefinition registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = CacheKeyPartGenerator.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerCacheKeyPartGenerator(CacheKeyPartGenerator registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = HandlerMapping.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerHandlerMapping(HandlerMapping registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = ProviderFactory.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerProviderFactory(ProviderFactory registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = AbstractServletFilter.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerAbstractServletFilter(AbstractServletFilter registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = SearchProvider.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerSearchProvider(SearchProvider registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = ModuleGWTResources.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerModuleGWTResources(ModuleGWTResources registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = PasswordDigester.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterOSGIService")
    public void registerPasswordDigester(PasswordDigester registrar) {
        registerOSGIService(registrar);
    }

    @Reference(service = LoginUrlProvider.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterLoginUrlProvider")
    public void registerLoginUrlProvider(LoginUrlProvider registrar) {
        LoginConfig.getInstance().osgiBind(registrar);
    }
    public void unregisterLoginUrlProvider(LoginUrlProvider service) {
        LoginConfig.getInstance().osgiUnbind(service);
    }

    @Reference(service = LogoutUrlProvider.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterLogoutUrlProvider")
    public void registerLogoutUrlProvider(LogoutUrlProvider registrar) {
        LogoutConfig.getInstance().osgiBind(registrar);
    }
    public void unregisterLogoutUrlProvider(LogoutUrlProvider service) {
        LogoutConfig.getInstance().osgiUnbind(service);
    }

    @Reference(service = HttpListener.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterHttpListener")
    public void registerHttpListener(HttpListener registrar) {
        ((HttpListenersRegistry) SpringContextSingleton.getBean("HttpListenersRegistry")).addListener(registrar);
    }
    public void unregisterHttpListener(HttpListener service) {
        ((HttpListenersRegistry) SpringContextSingleton.getBean("HttpListenersRegistry")).removeListener(service);
    }

    @Reference(service = JahiaEventListener.class,  policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY, unbind = "unregisterJahiaEventListener")
    public void registerJahiaEventListener(JahiaEventListener registrar) {
        ((JahiaEventServiceImpl) SpringContextSingleton.getBean("jahiaEventService")).addEventListener(registrar);
    }
    public void unregisterJahiaEventListener(JahiaEventListener service) {
        ((JahiaEventServiceImpl) SpringContextSingleton.getBean("jahiaEventService")).removeEventListener(service);
    }
}
