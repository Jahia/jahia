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
package org.jahia.services.seo.urlrewrite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.utils.FileUtils;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

/**
 * Utility class that calculates checksum for the specified Web resources.
 *
 * @author Sergiy Shyrkov
 */
public class ResourceChecksumCalculator implements ApplicationListener<TemplatePackageRedeployedEvent> {

    private static Map<String, String> checksums = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(ResourceChecksumCalculator.class);

    /**
     * Flushes internal checksum cache for resources.
     */
    public static void flushChecksumCache() {
        checksums.clear();
    }

    private static InputStream getResourceAsStream(String resourcePath) {
        try {
            return WebUtils.getResourceAsStream(resourcePath);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Calculates the checksum for the specified resource and sets it as a request attribute.
     *
     * @param request the current request
     * @param ctx current request context
     * @param resourcePath the resource path to calculate checksum for
     */
    public void calculateChecksum(HttpServletRequest request, String ctx, String resourcePath) {
        request.setAttribute("ResourceChecksumCalculator.checksum", getChecksum(request, resourcePath));
    }

    private String getChecksum(HttpServletRequest request, String resourcePath) {
        String checksum = checksums.get(resourcePath);
        if (checksum == null) {
            long startTime = System.currentTimeMillis();
            @SuppressWarnings("resource") InputStream resourceAsStream = getResourceAsStream(resourcePath);
            checksum = resourceAsStream != null ? FileUtils.calculateDigest(resourceAsStream) : "0";
            checksums.put(resourcePath, checksum);
            if (logger.isDebugEnabled()) {
                logger.debug("Checksum for resource {} calculated in {} ms: {}",
                        new Object[] { resourcePath, System.currentTimeMillis() - startTime, checksum });
            }
        }
        return checksum;
    }

    @Override
    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        logger.debug("Event received: {}", event);
        String prefix = "/modules/";
        if (event.getSource() instanceof String) {
            prefix = prefix + (String) event.getSource() + "/";
        }
        for (Iterator<Map.Entry<String, String>> iterator = checksums.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, String> entry = iterator.next();
            if (entry.getKey().startsWith(prefix)) {
                logger.debug("Invalidating cached checksum entry for resource: {}", entry.getKey());
                iterator.remove();
            }
        }
    }

}
