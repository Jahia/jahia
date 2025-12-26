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
package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderServiceAware;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.jcr.*;

import java.text.MessageFormat;
import java.util.*;

/**
 * Service to render node
 *
 * @author toto
 */
public class RenderService {

    public static final String RENDER_SERVICE_TEMPLATES_CACHE = "RenderService.TemplatesCache";

    private static final Logger logger = LoggerFactory.getLogger(RenderService.class);

    public static class RenderServiceBeanPostProcessor implements BeanPostProcessor {
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RenderServiceAware) {
                ((RenderServiceAware) bean).setRenderService(getInstance());
            }
            if (bean instanceof RenderFilter) {
                logger.debug("Registering render filter {}", bean.getClass().getName());
                getInstance().addFilter((RenderFilter) bean);
            }
            return bean;
        }
    }

    private void addFilter(RenderFilter renderFilter) {
        if (filters.contains(renderFilter)) {
            filters.remove(renderFilter);
        }
        filters.add(renderFilter);
    }

    private static volatile RenderService instance;

    public static RenderService getInstance() {
        if (instance == null) {
            synchronized (RenderService.class) {
                if (instance == null) {
                    instance = new RenderService();
                }
            }
        }
        return instance;
    }

    private RenderTimeMonitor renderTimeMonitor;

    private Collection<TemplateResolver> templateResolvers = new ArrayList<>();

    private JahiaTemplateManagerService templateManagerService;

    private Collection<ScriptResolver> scriptResolvers;

    private List<RenderFilter> filters = new LinkedList<RenderFilter>();


    public void setScriptResolvers(Collection<ScriptResolver> scriptResolvers) {
        this.scriptResolvers = scriptResolvers;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public Collection<ScriptResolver> getScriptResolvers() {
        return scriptResolvers;
    }

    public Collection<TemplateResolver> getTemplateResolvers() {
        return templateResolvers;
    }

    public void setTemplateResolvers(Collection<TemplateResolver> templateResolvers) {
        this.templateResolvers = templateResolvers;
    }


    public void start() throws JahiaInitializationException {

    }

    public void stop() throws JahiaException {

    }

    /**
     * Render a specific resource and returns it as a StringBuffer.
     *
     * @param resource Resource to display
     * @param context  The render context
     * @return The rendered result
     * @throws RenderException in case of rendering issues
     */
    public String render(Resource resource, RenderContext context) throws RenderException {
        /*
            TODO BACKLOG-6561: used for retro compatibility for AggregateCacheFilter
                the stack of resources is now handle internally by the AggregateFilter
                we need to remove this the day we stop supporting the AggregateCacheFilter implementation
         */

        if (context.getResourcesStack().contains(resource)) {
            String resourceMessage = Messages.getInternal("label.render.loop", context.getUILocale());
            String formattedMessage = MessageFormat.format(resourceMessage, resource.getPath());
            logger.warn("Loop detected while rendering resource {}. Please check your content structure and references.", resource.getPath());
            return formattedMessage;
        }

        if (renderTimeMonitor != null) {
            renderTimeMonitor.track(context.getRequest());
        }

        String output = getRenderChainInstance().doFilter(context, resource);

        if (renderTimeMonitor != null) {
            renderTimeMonitor.monitor(resource, context);
        }

        return output;
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple JSPScript.
     * <p/>
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param context rendering context instance
     * @return An executable script
     * @throws RepositoryException in case of JCR-related errors
     * @throws TemplateNotFoundException in case the template could not be found for the supplied resource
     */
    public Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            try {
                Script s = scriptResolver.resolveScript(resource, context);
                if (s != null) {
                    return s;
                }
            } catch (TemplateNotFoundException tnfe) {
                // skip this resolver and continue with the next one
            }
        }
        throw new TemplateNotFoundException("Unable to find the template for resource " + resource);
    }


    public boolean hasView(JCRNodeWrapper node, String key, String templateType, RenderContext renderContext) {
        try {
            if (hasView(node.getPrimaryNodeType(), key, renderContext.getSite(), templateType, renderContext)) {
                return true;
            }
            for (ExtendedNodeType type : node.getMixinNodeTypes()) {
                if (hasView(type, key, renderContext.getSite(), templateType, renderContext)) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            // ignore and continue resolution
        }
        return false;
    }

    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site, String templateType, RenderContext renderContext) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasView(nt, key, site, templateType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new instance of the {@link RenderChain} with all filters.
     *
     * @return a new instance of the {@link RenderChain} with all filters
     */
    public RenderChain getRenderChainInstance() {
        return new RenderChain(filters, templateManagerService.getRenderFilters());
    }

    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site, String templateType) {
        SortedSet<View> set = new TreeSet<View>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getViewsSet(nt, site, templateType));
        }
        return set;
    }

    public boolean hasTemplate(String templateName, ExtendedNodeType nodeType, Set<String> templatePackages) throws RepositoryException {
        if (nodeType == null) {
            throw new IllegalArgumentException("Node type is null");
        }
        if (StringUtils.isEmpty(templateName)) {
            throw new IllegalArgumentException("Template path is either null or empty");
        }
        if (templatePackages == null || templatePackages.isEmpty()) {
            throw new IllegalArgumentException("The template/module set to check is empty");
        }

        for (TemplateResolver templateResolver : templateResolvers) {
            if (templateResolver.hasTemplate(templateName, nodeType, templatePackages)) {
                return true;
            }
        }
        return false;
    }

    public Template resolveTemplate(Resource resource, RenderContext renderContext) throws RepositoryException {
        Template selectedTemplate = null;
        int highestPriority = Integer.MIN_VALUE;
        // Loop over templates resolvers to find the matching templates
        for (TemplateResolver templateResolver : templateResolvers) {
            Template template = templateResolver.resolveTemplate(resource, renderContext);
            if (template != null) {
                int priority = template.getPriority();
                // Track the template with the highest priority across all resolvers
                if (priority > highestPriority) {
                    highestPriority = priority;
                    selectedTemplate = template;
                }
            }
        }

        return selectedTemplate;
    }

    public void flushCache(String modulePath) {
        for (TemplateResolver templateResolver : templateResolvers) {
            templateResolver.flushCache(modulePath);
        }
    }

    public void setRenderTimeMonitor(RenderTimeMonitor renderChainMonitor) {
        this.renderTimeMonitor = renderChainMonitor;
    }

}
