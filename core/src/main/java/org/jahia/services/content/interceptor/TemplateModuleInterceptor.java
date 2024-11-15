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
