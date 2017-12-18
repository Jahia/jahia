/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.interceptor;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueFactoryImpl;
import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;

/**
 * Intercepts reading of reference/weakreference property values to translate the path from modules to site.
 */
public class TemplateModuleInterceptor extends BaseInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TemplateModuleInterceptor.class);

    public static final ThreadLocal<RenderContext> renderContextThreadLocal = new ThreadLocal<RenderContext>();

    private static final int TEMPLATES_TOKEN_POSITION = 3;

    @Override
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException,
            RepositoryException {

        RenderContext renderContext = renderContextThreadLocal.get();
        if (renderContext != null) {
            String contextSitePath = renderContext.getSite().getPath();
            if (StringUtils.startsWith(contextSitePath, "/sites")) {
                // we are under a site
                String propertyPath = property.getPath();
                if (propertyPath.startsWith("/modules/")) {
                    // node is under /modules
                    String[] propertyPathTokens = StringUtils.split(propertyPath, "/", TEMPLATES_TOKEN_POSITION + 2);
                    if (propertyPathTokens.length >= (TEMPLATES_TOKEN_POSITION + 2)
                            && "templates".equals(propertyPathTokens[TEMPLATES_TOKEN_POSITION])) {
                        // our node is under "templates" node in a module
                        try {
                            String referencePath = property.getSession().getNodeByIdentifier(storedValue.getString())
                                    .getPath();
                            if (referencePath.startsWith("/modules/")) {
                                // target is also in a module
                                String[] path = StringUtils.split(referencePath, "/");
                                if (path.length >= TEMPLATES_TOKEN_POSITION
                                        && !"templates".equals(path[TEMPLATES_TOKEN_POSITION])) {
                                    StringBuilder sitePath = new StringBuilder(64);
                                    sitePath.append(contextSitePath);
                                    for (int i = TEMPLATES_TOKEN_POSITION; i < path.length; i++) {
                                        sitePath.append("/").append(path[i]);
                                    }
                                    return JCRValueFactoryImpl.getInstance().createValue(
                                            property.getSession().getNode(sitePath.toString()));
                                }
                            }
                        } catch (PathNotFoundException e) {
                            logger.warn("Cannot get reference in local site " +e.getMessage());
                            renderContext.getRequest().setAttribute("expiration", "0");
                        } catch (ItemNotFoundException e) {
                            // referenced node not available
                        }
                    }
                }
            }
        }
        return storedValue;
    }
}