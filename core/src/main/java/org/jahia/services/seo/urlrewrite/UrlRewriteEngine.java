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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.seo.urlrewrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.tuckey.web.filters.urlrewrite.RewrittenOutboundUrl;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * URL rewriter engine.
 *
 * @author Sergiy Shyrkov
 */
class UrlRewriteEngine extends UrlRewriter {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteEngine.class);

    private static Configuration getConfiguration(ServletContext context, Resource[] confLocations) {
        Configuration cfg = null;
        if (confLocations == null || confLocations.length == 0) {
            logger.warn("No configuration resource location specified for"
                    + " the URL rewrite engine. Using empty one.");
            return new Configuration();
        }
        try {
            cfg = new Configuration(context, confLocations);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return cfg;
    }

    /**
     * Initializes an instance of this class.
     *
     * @param context
     *            current servlet context
     * @param confLocations
     *            the URL rewriter configuration resource location
     */
    public UrlRewriteEngine(ServletContext context, Resource[] confLocations) {
        super(getConfiguration(context, confLocations));
        if (confLocations != null) {
            logger.info("Loaded URL rewrite rules from {}",java.util.Arrays.asList(confLocations));
        }
    }

    public String rewriteOutbound(String url, HttpServletRequest request,
                                  HttpServletResponse response) throws IOException, ServletException,
            InvocationTargetException {

        RewrittenOutboundUrl rou = processEncodeURL(response, request, false, url);
        if (rou == null) {
            return response.encodeURL(url);
        }
        if (rou.isEncode()) {
            rou.setTarget(response.encodeURL(rou.getTarget()));
        }
        return processEncodeURL(response, request, true, rou.getTarget()).getTarget();

    }
}
