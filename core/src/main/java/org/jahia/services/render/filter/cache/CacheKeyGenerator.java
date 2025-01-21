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
package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.Properties;

/**
 * Describes the module output cache key generator.
 *
 * @author Sergiy Shyrkov
 */
public interface CacheKeyGenerator {

    /**
     * Generates the output cache key based on the provided data.
     *
     * @param resource the current resource being rendered
     * @param renderContext the current rendering context
     * @param properties Fragment properties
     * @return the output cache key based on the provided data
     */
    String generate(Resource resource, RenderContext renderContext, Properties properties);

    /**
     * Parses the specified key into separate fields.
     *
     * The map returned will specifically be passed as the first argument to the prepareContextForContentGeneration and restoreContextAfterContentGeneration methods,
     * so implementations of these three methods should be coordinated taking this fact into account.
     *
     * @param key the cache key to be decomposed
     * @return a map with key field values
     */
    Map<String, String> parse(String key);

    /**
     * @deprecated Not used anymore
     */
    @Deprecated(since = "7.2.0.0", forRemoval = true)
    String replaceField(String key, String fieldName, String newValue) ;

    /**
     * Return the cache key part generator corresponding to the given field
     *
     * @param field the given field
     * @return the CacheKeyPartGenerator if found, null if not
     */
    CacheKeyPartGenerator getPartGenerator(String field);

    /**
     * Transform the fragment key into the final key used for caching the fragment
     *
     * @param renderContext the current render context
     * @param key the key to be transformed
     * @return the transformed key
     */
    String replacePlaceholdersInCacheKey(RenderContext renderContext, String key);

    /**
     * Before generating a sub-fragment during aggregation, this function will be called to modify the render context.
     *
     * This function returns a map that represents the state of the render context as it was at the moment of the method invocation.
     * The map retrieved is to be passed as an argument to the restoreContextAfterContentGeneration method when generation of the
     * sub-fragment completes.
     *
     * @param keyParts The fragment key parsed, typically a map returned by the parse method
     * @param resource The current rendered resource
     * @param renderContext The current renderContext
     * <p>
     * @deprecated Render chain V2 is deprecated and will be removed in the next major release (8.3.0.0).
     * <p>
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    Map<String, Object> prepareContextForContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext);

    /**
     * After generating a sub-fragment during aggregation, this function will be called to restore the render context.
     *
     * @param keyParts The fragment key parsed, typically a map returned by the parse method
     * @param resource The current rendered resource
     * @param renderContext The current renderContext
     * @param original The map previously retrieved via prepareContextForContentGeneration() invocation
     * <p>
     * @deprecated Render chain V2 is deprecated and will be removed in the next major release (8.3.0.0).
     * <p>
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    void restoreContextAfterContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext, Map<String, Object> original);

    /**
     * Get all cache attributes that need to be applied on this fragment and that will impact key generation.
     * <p>
     * @deprecated Render chain V2 is deprecated and will be removed in the next major release (8.3.0.0).
     * <p>
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException;
}
