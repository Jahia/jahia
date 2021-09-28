/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BundleObserver that handle .dsl rule files
 *
 * This observer have a particularity, it is maintaining in memory the entries.
 * So the removing of the entries is done automatically without the need to scan the bundle.
 */
public class RulesDSLBundleObserver  implements BundleObserver<URL> {

    private static final Logger logger = LoggerFactory.getLogger(RulesDSLBundleObserver.class);

    private TemplatePackageRegistry templatePackageRegistry;

    private final Map<Long, List<URL>> dslCachePerBundleId;

    public RulesDSLBundleObserver() {
        super();
        templatePackageRegistry = ((JahiaTemplateManagerService) SpringContextSingleton
                .getBean("JahiaTemplateManagerService")).getTemplatePackageRegistry();
        dslCachePerBundleId = new ConcurrentHashMap<>();
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        if (urls.isEmpty()) {
            return;
        }
        String bundleName = BundleUtils.getDisplayName(bundle);
        for (URL url : urls) {
            BundleResource bundleResource = new BundleResource(url, bundle);
            try {
                JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);
                module.setRulesDescriptorFile(bundleResource.getURL().getPath().substring(1));
                cacheDslForBundle(bundle, url);

                for (RulesListener listener : RulesListener.getInstances()) {
                    listener.addRulesDescriptor(bundleResource, module);
                }
                logger.info("Registered rule file descriptor {} for bundle {}", url, bundleName);
            } catch (IOException e) {
                throw new JahiaRuntimeException("Error registering rule file descriptor " + url + " for bundle " + bundle, e);
            }
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        List<URL> cachedUrls = dslCachePerBundleId.get(bundle.getBundleId());
        if (cachedUrls != null && !cachedUrls.isEmpty()) {
            for (RulesListener listener : RulesListener.getInstances()) {
                for (URL url : cachedUrls) {
                    if(listener.removeRulesDescriptor(new BundleResource(url, bundle))){
                        logger.info("Removing rule file descriptor {}", url);
                    }
                }
            }
            dslCachePerBundleId.remove(bundle.getBundleId());
        }
    }

    private void cacheDslForBundle(Bundle bundle, URL url) {
        List<URL> cachedUrls = dslCachePerBundleId.get(bundle.getBundleId());
        if (cachedUrls == null) {
            cachedUrls = new ArrayList<>();
        }
        cachedUrls.add(url);
        dslCachePerBundleId.put(bundle.getBundleId(), cachedUrls);
    }
}
