/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
            JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);
            for (RulesListener listener : RulesListener.getInstances()) {
                for (URL url : cachedUrls) {
                    if(listener.removeRulesDescriptor(new BundleResource(url, bundle))){
                        logger.info("Removing rule file descriptor {}", url);
                    }
                }
                listener.removeRules(module);
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
