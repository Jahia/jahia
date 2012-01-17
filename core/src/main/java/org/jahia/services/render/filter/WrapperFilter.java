/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
        if (renderContext.isAjaxRequest()) {
            return previousOut;
        }
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
            if (service.hasView(node, wrapper)) {
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
