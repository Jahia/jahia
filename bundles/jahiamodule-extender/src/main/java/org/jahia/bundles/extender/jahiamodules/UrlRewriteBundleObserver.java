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
