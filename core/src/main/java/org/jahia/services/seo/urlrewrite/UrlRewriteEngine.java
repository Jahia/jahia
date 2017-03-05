/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
