/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.Properties;

/**
 * Interface used to implement custom Cache key part
 *
 * User: toto
 * Date: 11/20/12
 * Time: 12:20 PM
 */
public interface CacheKeyPartGenerator {

    /**
     * @return The key to identify the part generator
     */
    String getKey();

    /**
     * Get value of this given key part.
     *
     * @param resource the current rendered resource
     * @param renderContext the current renderContext
     * @param properties the current fragment properties
     * @return the value for this part of the key
     */
    String getValue(Resource resource, RenderContext renderContext, Properties properties);

    /**
     * Replace placeholders in the keyPart to construct the final key used to store the fragment in the cache.
     * This function will be called every time to construct the final key and try get fragment from cache,
     * that's why this operation need to be as light as possible.
     *
     * @param renderContext the current render context
     * @param keyPart the key part, a value returned by the getValue(...) function
     * @return the final key where plecaholders replaced with actual values
     */
    String replacePlaceholders(RenderContext renderContext, String keyPart);
}
