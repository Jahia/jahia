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

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.text.ParseException;
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
     *
     * @param resource the current resource being rendered
     * @param renderContext the current rendering context
     * @param properties
     * @return the output cache key based on the provided data
     */
    String generate(Resource resource, RenderContext renderContext, Properties properties);

    /**
     * Parses the specified key into separate fields.
     * 
     * @param key the cache key to be decomposed
     * @return a map with key field values
     * @throws ParseException in case of a malformed key
     */
    Map<String, String> parse(String key);

    /**
     * Decomposes the key, replaces the specified field with the provided value
     * and generates the new key.
     * 
     * @param key the cache key to be decomposed
     * @param fieldName the name of the field to be replaced
     * @param newValue the value of the field to be used in a new key
     * @return the newly generated key, based on the old one, replacing the
     *         specified field
     * @throws ParseException in case of a malformed key
     */
    String replaceField(String key, String fieldName, String newValue) ;

    CacheKeyPartGenerator getPartGenerator(String field);

    String replacePlaceholdersInCacheKey(RenderContext renderContext, String key);
}
