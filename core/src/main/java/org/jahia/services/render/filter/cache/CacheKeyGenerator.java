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
    @Deprecated
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
     * @return A map that represents the original state of the render context
     */
    Map<String, Object> prepareContextForContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext);

    /**
     * After generating a sub-fragment during aggregation, this function will be called to restore the render context.
     *
     * @param keyParts The fragment key parsed, typically a map returned by the parse method
     * @param resource The current rendered resource
     * @param renderContext The current renderContext
     * @param original The map previously retrieved via prepareContextForContentGeneration() invocation
     */
    void restoreContextAfterContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext, Map<String, Object> original);

    /**
     * Get all cache attributes that need to be applied on this fragment and that will impact key generation.
     */
    Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException;
}
