/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
                renderContext.getRequest().setAttribute("previousTemplate", template);
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