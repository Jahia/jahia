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

package org.jahia.services.seo.urlrewrite;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.LRUMap;
import org.jahia.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that calculates checksum for the specified Web resources.
 *
 * @author Sergiy Shyrkov
 */
public class ResourceChecksumCalculator {

    @SuppressWarnings("unchecked")
    private static Map<String, String> checksums = Collections.synchronizedMap(new LRUMap(1000));

    private static final Logger logger = LoggerFactory.getLogger(ResourceChecksumCalculator.class);

    /**
     * Flushes internal checksum cache for resources.
     */
    public static void flushChecksumCache() {
        checksums.clear();
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
            @SuppressWarnings("resource") InputStream resourceAsStream = request.getServletContext().getResourceAsStream(resourcePath);
            checksum = resourceAsStream != null ? FileUtils.calculateDigest(resourceAsStream) : "0";
            checksums.put(resourcePath, checksum);
            if (logger.isDebugEnabled()) {
                logger.debug("Checksum for resource {} calculated in {} ms: {}",
                        new Object[] { resourcePath, System.currentTimeMillis() - startTime, checksum });
            }
        }
        return checksum;
    }
}
