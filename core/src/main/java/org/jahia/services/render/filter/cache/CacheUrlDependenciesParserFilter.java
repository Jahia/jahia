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
