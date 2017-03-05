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

import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.util.List;

/**
 * @author Christophe Laprun
 */
public class UrlRewriteBundleObserver implements BundleObserver<URL> {
    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteBundleObserver.class);
    private UrlRewriteService rewriteService;

    public UrlRewriteBundleObserver() {
        super();
        rewriteService = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> entries) {
        if (!entries.isEmpty()) {
            final String name = BundleUtils.getDisplayName(bundle);
            for (URL url : entries) {
                // create associated Resource
                Resource resource = new BundleResource(url, bundle);
                final String path = url.getPath();
                if (path.contains("seo-")) {
                    // SEO rule
                    this.rewriteService.addSeoConfigurationResource(resource);
                    logger.info("Adding SEO rewrite configuration " + url + " for bundle " + name);
                } else if (path.contains("last-")) {
                    // last rule
                    this.rewriteService.addLastConfigurationResource(resource);
                    logger.info("Adding last rewrite configuration " + url + " for bundle " + name);
                } else {
                    // regular rule
                    this.rewriteService.addConfigurationResource(resource);
                    logger.info("Adding rewrite configuration " + url + " for bundle " + name);
                }

            }
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> entries) {
        if (!entries.isEmpty()) {
            final String name = BundleUtils.getDisplayName(bundle);
            for (URL url : entries) {
                // create associated Resource
                Resource resource = new BundleResource(url, bundle);
                final String path = url.getPath();
                if (path.contains("seo-")) {
                    // SEO rule
                    this.rewriteService.removeSeoConfigurationResource(resource);
                    logger.info("Removed SEO rewrite configuration " + url + " for bundle " + name);
                } else if (path.contains("last-")) {
                    // last rule
                    this.rewriteService.removeLastConfigurationResource(resource);
                    logger.info("Removed last rewrite configuration " + url + " for bundle " + name);
                } else {
                    // regular rule
                    this.rewriteService.removeConfigurationResource(resource);
                    logger.info("Removed rewrite configuration " + url + " for bundle " + name);
                }

            }
        }
    }
}
