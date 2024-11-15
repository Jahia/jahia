/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.slf4j.Logger;

/**
 * WrapperFilter
 * <p/>
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 */
public class TemplateNodeFilter extends AbstractFilter {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateNodeFilter.class);

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute("skipWrapper") == null && !renderContext.isAjaxRequest()) {
            chain.pushAttribute(renderContext.getRequest(), "inWrapper", Boolean.TRUE);
            Template template = null;
            Template previousTemplate = null;
            if (renderContext.getRequest().getAttribute("templateSet") == null) {
                template = service.resolveTemplate(resource, renderContext);
                if(template==null) {
                    throw new TemplateNotFoundException(resource.getTemplate());
                }
                renderContext.getRequest().setAttribute("templateSet", Boolean.TRUE);
                if (logger.isDebugEnabled()) {
                    logger.debug("Template set to : " + template.serialize() + " for resource " + resource);
                }
            } else if (renderContext.getRequest().getAttribute("cachedTemplate") != null) {
                template = (Template) renderContext.getRequest().getAttribute("cachedTemplate");
                renderContext.getRequest().removeAttribute("cachedTemplate");
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Restoring cached template to : " + template.serialize() + " for resource " + resource);
                }
            } else {
                previousTemplate = (Template) renderContext.getRequest().getAttribute("previousTemplate");
                if (previousTemplate != null) {
                    template = previousTemplate.next;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using Previous Template : " + previousTemplate.serialize() + " for resource " + resource);
                        if (template != null) {
                            logger.debug("Setting Template to use to : " + template.serialize() + " for resource " + resource);
                        } else {
                            logger.debug("Template has been set to null for resource " + resource);
                        }
                    }
                }

            }

            if (template != null && template.node != null) {
                JCRNodeWrapper templateNode = resource.getNode().getSession().getNodeByIdentifier(template.node);

                Template last = template;
                while (last.next != null) {
                    last = last.next;
                }
                JCRNodeWrapper currentTemplate = last.node != null ? resource.getNode().getSession().getNodeByIdentifier(last.node) : null;

                renderContext.getRequest().setAttribute("previousTemplate", template);
                renderContext.getRequest().setAttribute("currentTemplateNode", currentTemplate);
                renderContext.getRequest().setAttribute("wrappedResource", resource);
                Resource wrapperResource = new Resource(templateNode,
                        resource.getTemplateType(), template.view, Resource.CONFIGURATION_WRAPPER);
                if (service.hasView(templateNode, template.getView(), resource.getTemplateType(), renderContext)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Calling render service with template : " + template.serialize() +
                                " templateNode path : " + templateNode.getPath() + " for wrapperresource " +
                                wrapperResource);
                    }
                    String output = RenderService.getInstance().render(wrapperResource, renderContext);
                    renderContext.getRequest().setAttribute("previousTemplate", previousTemplate);

                    return output;
                } else {
                    logger.warn("Cannot get wrapper " + template);
                }
            }
        } else if (renderContext.isAjaxRequest() && resource.getContextConfiguration().equals(Resource.CONFIGURATION_PAGE)) {
            String i = renderContext.getRequest().getParameter("jarea");
            if (i != null) {
                JCRNodeWrapper area = resource.getNode().getSession().getNodeByUUID(i);
                Resource wrapperResource = new Resource(area, resource.getTemplateType(), null, Resource.CONFIGURATION_MODULE);
                String output = RenderService.getInstance().render(wrapperResource, renderContext);
                return output;
            }
        }
        chain.pushAttribute(renderContext.getRequest(), "inWrapper",
                (renderContext.isAjaxRequest()) ? Boolean.TRUE :
                        renderContext.getRequest().getAttribute("inArea") != null ? Boolean.TRUE : Boolean.FALSE);
        return null;
    }
}
