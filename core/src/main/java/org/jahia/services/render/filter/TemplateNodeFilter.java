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
package org.jahia.services.render.filter;

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.slf4j.Logger;

/**
 * TemplateNodeFilter
 * <p/>
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 */
public class TemplateNodeFilter extends AbstractFilter {

    /**
     * cachedTemplate: This is a legacy attribute that is no longer used internally.
     * It was originally intended to restore the template from a cache key (not used anymore).
     * Currently, it is only utilized by other modules, such as:
     * - jExperience page personalization/optimization: used to modify the template and inject it before the current filter.
     * Ideally, this attribute should be removed in the future if jExperience changes its implementation for page personalization/optimization.
     * (variable is named: ATTR_OVERRIDE_TEMPLATE, to better reflect what it does as of today)
     */
    public static final String ATTR_OVERRIDE_TEMPLATE = "cachedTemplate";

    /**
     * previousTemplate: Used to store the previous template in the request.
     * This attribute is tied to the template hierarchy system, which is used to resolve area content for relative areas.
     * For more details, refer to the AreaTag source code explanation about this system.
     * (variable is named: ATTR_RESOLVED_TEMPLATE, to better reflect what it does as of today)
     */
    public static final String ATTR_RESOLVED_TEMPLATE = "previousTemplate";

    /**
     * templateSet: A flag used to indicate that the template has been set for the current request.
     * This helps avoid setting the template multiple times and prevents multiple resolution processes.
     */
    public static final String ATTR_TEMPLATE_SET = "templateSet";

    /**
     * skipWrapper: Indicates that the template node wrapper logic should be entirely skipped.
     * This is primarily used for:
     * - AreaTag: to bypass the template node wrapper logic when rendering area content.
     */
    public static final String ATTR_SKIP_TEMPLATE_NODE_WRAPPER = "skipWrapper";

    /**
     * inWrapper: Indicates that the rendering process is within a template wrapper logic.
     * This attribute is mainly used for:
     * - ModuleTag: to identify when a template:module is used directly in a template node, making it non-editable.
     * - mainResourceDisplay.jsp: to render custom HTML in the studio based on the value of this attribute.
     * It serves a purpose similar to ATTR_IN_AREA but is set earlier in the process.
     */
    public static final String ATTR_IN_TEMPLATE_NODE_WRAPPER = "inWrapper";

    /**
     * inArea: Indicates that the rendering process is within an area.
     * Note: This attribute is only used in specific cases, making its usage somewhat misleading:
     * - When an area renders its content from a template node (i.e., the area resolves its node from the template node instead of the main resource).
     * - When an absolute area renders its content with the option `limitedAbsoluteAreaEdit` (restricting editing to the main resource containing the absolute area).
     * This attribute is primarily used to determine if the content is editable. Similar to inWrapper, content is not editable when
     * the area is rendering template nodes or absolute areas with restricted editing.
     */
    public static final String ATTR_IN_AREA = "inArea";

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateNodeFilter.class);

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute(ATTR_SKIP_TEMPLATE_NODE_WRAPPER) == null) {
            chain.pushAttribute(renderContext.getRequest(), ATTR_IN_TEMPLATE_NODE_WRAPPER, Boolean.TRUE);
            Template template = null;
            Template previousTemplate = null;
            if (renderContext.getRequest().getAttribute(ATTR_TEMPLATE_SET) == null) {
                template = service.resolveTemplate(resource, renderContext);
                if(template == null) {
                    throw new TemplateNotFoundException(resource.getTemplate());
                }
                renderContext.getRequest().setAttribute(ATTR_TEMPLATE_SET, Boolean.TRUE);
                if (logger.isDebugEnabled()) {
                    logger.debug("Template set to : {} for resource {}", template.serialize(), resource);
                }
            } else if (renderContext.getRequest().getAttribute(ATTR_OVERRIDE_TEMPLATE) != null) {
                template = (Template) renderContext.getRequest().getAttribute(ATTR_OVERRIDE_TEMPLATE);
                renderContext.getRequest().removeAttribute(ATTR_OVERRIDE_TEMPLATE);
                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring cached template to : {} for resource {}", template.serialize(), resource);
                }
            } else {
                previousTemplate = (Template) renderContext.getRequest().getAttribute(ATTR_RESOLVED_TEMPLATE);
                if (previousTemplate != null) {
                    template = previousTemplate.next;
                    throw new JahiaRuntimeException("This is an experimental exception to test if this code is actually dead or not, " +
                            "if you are reading this, please contact Kevan. Some data: " + previousTemplate.serialize() + " for resource " + resource);
                    /* if (logger.isDebugEnabled()) {
                        logger.debug("Using Previous Template : " + previousTemplate.serialize() + " for resource " + resource);
                        if (template != null) {
                            logger.debug("Setting Template to use to : " + template.serialize() + " for resource " + resource);
                        } else {
                            logger.debug("Template has been set to null for resource " + resource);
                        }
                    }*/
                }
            }

            if (template != null) {
                // Even if previousTemplate is a concept born with the JCR node templating mechanism, it's still useful for external templates
                // Example: jExperience page perso, pushing a template.next in order route areas of the rendered page to the right variant.
                renderContext.getRequest().setAttribute(ATTR_RESOLVED_TEMPLATE, template);

                if (template.isExternal()) {
                    // External template handling, we delegate the render chain to this external system by skipping rendering of template nodes
                    // Example: JS modules and JS views/templates
                    chain.pushAttribute(renderContext.getRequest(), ATTR_IN_TEMPLATE_NODE_WRAPPER, Boolean.FALSE);
                    return null;
                }

                // JCR template nodes handling, only if the template is a JCR template node
                if (template.node != null) {
                    JCRNodeWrapper templateNode = resource.getNode().getSession().getNodeByIdentifier(template.node);
                    Resource wrapperResource = new Resource(templateNode, resource.getTemplateType(), template.view, Resource.CONFIGURATION_WRAPPER);
                    if (service.hasView(templateNode, template.getView(), resource.getTemplateType(), renderContext)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Calling render service with template : {} templateNode path : {} for wrapperResource {}",
                                    template.serialize(), templateNode.getPath(), wrapperResource);
                        }
                        String output = RenderService.getInstance().render(wrapperResource, renderContext);
                        renderContext.getRequest().setAttribute(ATTR_RESOLVED_TEMPLATE, previousTemplate);

                        return output;
                    } else {
                        logger.warn("Cannot get wrapper {}", template);
                    }
                }
            }
        }

        chain.pushAttribute(renderContext.getRequest(), ATTR_IN_TEMPLATE_NODE_WRAPPER,
                renderContext.getRequest().getAttribute(ATTR_IN_AREA) != null);
        return null;
    }
}
