/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Map;
import java.util.Properties;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;

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
     * @param key the cache key to be decomposed
     * @return a map with key field values
     */
    Map<String, String> parse(String key);

    /**
     * @deprecated Not used anymore
     */
    String replaceField(String key, String fieldName, String newValue) ;

    /**
     * Return the cache key part generator corresponding to the given field
     *
     * @param field the given field
     * @return the CacheKeyPartGenerator if found, null if not
     */
    CacheKeyPartGenerator getPartGenerator(String field);

    /**
     * Transform the fragment key into the final key used for cache the fragment
     *
     * @param renderContext the current render context
     * @param key the key to be process
     * @return the transformed key
     */
    String replacePlaceholdersInCacheKey(RenderContext renderContext, String key);

    /**
     * Before generate a sub fragment during aggregation this function will be call to modify the context of the sub fragment
     * Sometime we store data in the cache key, to be able to re inject this data when a sub fragment need to be generate again.
     *
     * This function return a Map<String, Object>, corresponding to a map of Object that need to stored during sub fragment generation
     * indexed by "key" of all the key part generators. This Map will be pass again after the fragment generation
     * to the restoreContextAfterContentGeneration function
     *
     * @param keyParts The fragment key parsed, generally it's the result of the function parse(String key)
     * @param resource The current rendered resource
     * @param renderContext The current renderContext
     * @return The map of object that need to be stored during sub fragment generation
     */
    Map<String, Object> prepareContextForContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext);

    /**
     * After generate a sub fragment during aggregation this function will be call to restore the context, as it may be modify by the
     * prepareContextForContentGeneration() function before the generation.
     *
     * @param keyParts The fragment key parsed, generally it's the result of the function parse(String key)
     * @param resource The current rendered resource
     * @param renderContext The current renderContext
     * @param original The map of object that have been stored previously by prepareContextForContentGeneration()
     */
    void restoreContextAfterContentGeneration(Map<String, String> keyParts, Resource resource, RenderContext renderContext, Map<String, Object> original);

    /**
     * Get all cache attributes that need to be applied on this fragment and that will impact key generation. The
     * cache properties may come from the script properties file, or from the jmix:cache mixin (for cache.perUser
     * only).
     * <p/>
     * If the component is a list, the properties can also come from its hidden.load script properties.
     * <p/>
     * cache.perUser : is the cache entry specific for each user. Is set by j:perUser node property or cache.perUser
     * property in script properties
     * <p/>
     * cache.mainResource : is the cache entry dependant on the main resource. Is set by cache.mainResource property
     * in script properties, or automatically set if the component is bound.
     * <p/>
     * cache.requestParameters : list of request parameter that will impact the rendering of the resource. Is set
     * by cache.requestParameters property in script properties. ec,v,cacheinfo and moduleinfo are automatically added.
     * <p/>
     * cache.expiration : the expiration time of the cache entry. Can be set by the "expiration" request attribute,
     * j:expiration node property or the cache.expiration property in script properties.
     */
    Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException;
}
