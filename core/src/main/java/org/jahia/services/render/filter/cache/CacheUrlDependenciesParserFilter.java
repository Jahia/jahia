/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author rincevent
 * Created : 12/15/11
 */
public class CacheUrlDependenciesParserFilter implements HtmlTagAttributeTraverser.HtmlTagAttributeVisitor, InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(CacheUrlDependenciesParserFilter.class);
    private URLResolverFactory urlResolverFactory;
    private String liveDmsContext;

    /**
     * Applies the required modifications to the specified attribute if
     * needed.
     *
     *
     * @param value    the attribute value to be modified
     * @param context  current rendering context
     * @param tagName
     * @param attrName
     * @param resource current resource  @returns the modified attribute value
     */
    public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
        String contextConfiguration = resource.getContextConfiguration();
        if (context.isLiveMode()
                && contextConfiguration.equals(Resource.CONFIGURATION_MODULE)
                && StringUtils.isNotEmpty(value)) {

            if (value.startsWith(liveDmsContext)) {
                resource.getDependencies().add(
                        StringUtils.substringAfter(
                                value.contains("?") ? StringUtils.substringBefore(value, "?")
                                        : value, liveDmsContext));
            } else if (value
                    .startsWith(context.getRequest().getContextPath().length() > 0 ? context
                            .getRequest().getContextPath() + context.getServletPath() : context
                            .getServletPath())) {
                if (!value.equals(resource.getNode().getUrl())) {
                    try {
                        URLResolver urlResolver = urlResolverFactory.createURLResolver(value, context);
                        JCRNodeWrapper nodeWrapper = urlResolver.getNode();
                        resource.getDependencies().add(nodeWrapper.getCanonicalPath());
                    } catch (Exception e) {
                        logger.debug(e.getMessage(), e);
                    }
                }
            }
        }
        return value;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void afterPropertiesSet() throws Exception {
        liveDmsContext = Jahia.getContextPath() + "/files/live";
    }
}
