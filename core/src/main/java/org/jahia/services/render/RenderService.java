/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderServiceAware;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

/**
 * Service to render node
 *
 * @author toto
 */
public class RenderService {

    public static class RenderServiceBeanPostProcessor implements BeanPostProcessor {
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof RenderServiceAware) {
                ((RenderServiceAware) bean).setRenderService(getInstance());
            }
            if(bean instanceof RenderFilter) {
				logger.info("Registering render filter {}", bean.getClass().getName());
				getInstance().addFilter((RenderFilter) bean);
            }
            return bean;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(RenderService.class);

    private void addFilter(RenderFilter renderFilter) {
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

    private JahiaTemplateManagerService templateManagerService;

    private Collection<ScriptResolver> scriptResolvers;

    private List<RenderFilter> filters = new LinkedList<RenderFilter>();

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setScriptResolvers(Collection<ScriptResolver> scriptResolvers) {
        this.scriptResolvers = scriptResolvers;
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
        if (context.getResourcesStack().contains(resource)) {
            return "loop";
        }

        RenderChain renderChain = new RenderChain();
        renderChain.addFilters(filters);
        renderChain.addFilters(templateManagerService.getRenderFilters());
        String output = renderChain.doFilter(context, resource);

        return output;
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple JSPScript.
     * <p/>
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param context
     * @return An executable script
     * @throws RepositoryException
     * @throws IOException
     */
    public Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            Script s = scriptResolver.resolveScript(resource, context);
            if (s != null) {
                return s;
            }
        }
        return null;
    }


    public boolean hasTemplate(JCRNodeWrapper node, String key) {
        try {
            if (hasTemplate(node.getPrimaryNodeType(), key)) {
                return true;
            }
            for (ExtendedNodeType type : node.getMixinNodeTypes()) {
                if (hasTemplate(type, key)) {
                    return true;
                }
            }
        } catch (RepositoryException e) {

        }
        return false;
    }
    public boolean hasTemplate(ExtendedNodeType nt, String key) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasTemplate(nt, key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<Template> getAllTemplatesSet() {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getAllTemplatesSet());
        }
        return set;
    }

    public SortedSet<Template> getTemplatesSet(ExtendedNodeType nt) {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getTemplatesSet(nt));
        }
        return set;
    }

}
