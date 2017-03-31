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
package org.jahia.bundles.extender.jahiamodules;

import org.jahia.services.render.scripting.bundle.BundleSourceResourceResolver;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HttpContext that can look up resources in files as well as the default OSGi HttpContext
 */
public class FileHttpContext implements HttpContext {

    private static Logger logger = LoggerFactory.getLogger(FileHttpContext.class);

    private static URL NULL_URL;

    static {
        try {
            NULL_URL = new URL("http://");
        } catch (MalformedURLException e) {
            //
        }
    }

    private HttpContext parentHttpContext;

    private BundleSourceResourceResolver bundleSourceResourceResolver;

    private Map<String, URL> resourcesCache = new ConcurrentHashMap<String, URL>();

    public FileHttpContext(Bundle bundle, HttpContext parentHttpContext) {
        this.parentHttpContext = parentHttpContext;
        this.bundleSourceResourceResolver = new BundleSourceResourceResolver(bundle);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return parentHttpContext.handleSecurity(request, response);
    }

    @Override
    public URL getResource(String name) {
        if (bundleSourceResourceResolver.hasSourceURLs()) {
            URL resourceURL = bundleSourceResourceResolver.getResource(name);
            if (resourceURL != null) {
                return resourceURL;
            }
        }
        URL url = resourcesCache.get(name);
        if (url == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Getting resource: " + name);
            }
            url = parentHttpContext.getResource(name);
            resourcesCache.put(name, url != null ? url : NULL_URL);
        }
        return url != NULL_URL ? url : null;
    }

    @Override
    public String getMimeType(String name) {
        return parentHttpContext.getMimeType(name);
    }
}
