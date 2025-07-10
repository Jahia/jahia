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

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;
import org.slf4j.Logger;

/**
 * TemplateNodeFilter is managing the template node rendering logic.
 */
public class TemplateNodeFilter extends AbstractFilter {

    /**
     * <code>cachedTemplate</code>: This is a legacy attribute that is no longer used internally.
     * It was originally intended to restore the template from a cache key (not used anymore).
     * Currently, it is only utilized by other modules, such as:
     * <ul>
     * <li>jExperience page personalization/optimization: used to modify the template and inject it before the current filter.</li>
     * </ul>
     * Ideally, this attribute should be removed in the future if jExperience changes its implementation for page personalization/optimization.
     * <p>
     * (variable is named: <code>ATTR_OVERRIDE_TEMPLATE</code>, to better reflect what it does as of today)
     */
    public static final String ATTR_OVERRIDE_TEMPLATE = "cachedTemplate";

    /**
     * <code>previousTemplate</code>: Used to store the previous template in the request.
     * This attribute is tied to the template hierarchy system, which is used to resolve area content for relative areas.
     * For more details, refer to the AreaTag source code explanation about this system.
     * <p>
     * (variable is named: <code>ATTR_RESOLVED_TEMPLATE</code>, to better reflect what it does as of today)
     */
    public static final String ATTR_RESOLVED_TEMPLATE = "previousTemplate";

    /**
     * <code>templateSet</code>: A flag used to indicate that the template has been set for the current request.
     * This helps avoid setting the template multiple times and prevents multiple resolution processes.
     */
    public static final String ATTR_TEMPLATE_SET = "templateSet";

    /**
     * <code>skipWrapper</code>: Indicates that the template node wrapper logic should be entirely skipped.
     * This is primarily used for:
     * <ul>
     * <li>AreaTag: to bypass the template node wrapper logic when rendering area content.</li>
     * </ul>
     */
    public static final String ATTR_SKIP_TEMPLATE_NODE_WRAPPER = "skipWrapper";

    /**
     * <code>inWrapper</code>: Indicates that the current rendered resource belongs to a template node rather than current main resource.
     * <ul>
     * <li><code>false</code>: The current resource node is resolved from main resource page</li>
     * <li><code>true</code>: The current resource node is resolved from a template node.</li>
     * </ul>
     */
    public static final String ATTR_IN_WRAPPER = "inWrapper";

    /**
     * <code>inArea</code>: Managed and set by the AreaTag, when the AreaTag is about to call a new render chain for its resolved content list.
     * <ul>
     * <li><code>== null</code>: when the area content list is resolved in the current main resource (page).</li>
     * <li><code>!= null</code>: when the area content list is resolved somewhere else (absolute area or content list resolved in a parent template node in hierarchy).</li>
     * </ul>
     * Very similar to <code>inWrapper</code>, and actually used to calculate the <code>inWrapper</code> attribute.
     */
    public static final String ATTR_IN_AREA = "inArea";

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateNodeFilter.class);

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.getRequest().getAttribute(ATTR_SKIP_TEMPLATE_NODE_WRAPPER) == null) {
            chain.pushAttribute(renderContext.getRequest(), ATTR_IN_WRAPPER, Boolean.TRUE);
            Template template = null;

            // If the template is already set in the request, we use it, otherwise we resolve it
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
            }

            if (template != null) {
                // Even if previousTemplate is a concept born with the JCR node templating mechanism, it's still useful for external templates
                // Example: jExperience page perso, pushing a template.next in order route areas of the rendered page to the right variant.
                renderContext.getRequest().setAttribute(ATTR_RESOLVED_TEMPLATE, template);

                if (template.isExternal()) {
                    // External template handling, we delegate the render chain to this external system by skipping rendering of template nodes
                    // Example: JS modules and JS views/templates
                    chain.pushAttribute(renderContext.getRequest(), ATTR_IN_WRAPPER, Boolean.FALSE);
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

                        try {
                            return RenderService.getInstance().render(wrapperResource, renderContext);
                        } finally {
                            // Reset the resolvedTemplate attribute to null after rendering the wrapper
                            renderContext.getRequest().setAttribute(ATTR_RESOLVED_TEMPLATE, null);
                        }
                    } else {
                        logger.warn("Cannot get wrapper {}", template);
                    }
                }
            }
        }

        // AreaTag, when rendering his content list will set/remove the inArea attribute,
        // so that the TemplateNodeFilter can maintain the inWrapper attribute accordingly.
        chain.pushAttribute(renderContext.getRequest(), ATTR_IN_WRAPPER,
                renderContext.getRequest().getAttribute(ATTR_IN_AREA) != null);

        return null;
    }
}
