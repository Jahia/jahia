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

import org.jahia.services.render.*;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

/**
 * WrapperFilter
 *
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 *
 */
public class WrapperFilter extends AbstractFilter {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(WrapperFilter.class);

    private String wrapper;

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        JCRNodeWrapper node = resource.getNode();
        if (wrapper == null) {
            while (resource.hasWrapper()) {
                String wrapper = resource.popWrapper();
                previousOut = wrap(renderContext, resource, previousOut, node, wrapper);
            }
        } else {
            previousOut = wrap(renderContext, resource, previousOut, node, wrapper);
        }
        return previousOut;
    }

    private String wrap(RenderContext renderContext, Resource resource, String output, JCRNodeWrapper node,
                        String wrapper) throws RepositoryException {
        try {
//                renderContext.getRequest().setAttribute("wrappedResource", resource);
            Resource wrapperResource = new Resource(node, resource.getTemplateType(),
                    wrapper,
                    Resource.CONFIGURATION_WRAPPER);
            if (service.hasView(node, wrapper, resource.getTemplateType(), renderContext)) {
                Object wrappedContent = renderContext.getRequest().getAttribute("wrappedContent");
                try {
                    renderContext.getRequest().setAttribute("wrappedContent", output);
                    output = RenderService.getInstance().render(wrapperResource, renderContext);
                } finally {
                    renderContext.getRequest().setAttribute("wrappedContent", wrappedContent);
                }
            } else {
                logger.warn("Cannot get wrapper "+wrapper);
            }
        } catch (TemplateNotFoundException e) {
            logger.debug("Cannot find wrapper "+wrapper,e);
        } catch (RenderException e) {
            logger.error("Cannot execute wrapper "+wrapper,e);
        }
        return output;
    }
}
