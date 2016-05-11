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

import java.util.Properties;

/**
 * Interface used to implement custom Cache key part,
 * This allow to create a new element in cache keys
 *
 * User: toto
 * Date: 11/20/12
 * Time: 12:20 PM
 */
public interface CacheKeyPartGenerator {

    /**
     * The key to identify the part generator
     *
     * @return the key
     */
    String getKey();

    /**
     * get value for this given part generator, this operation can be heavy and read JCR because it won't be construct again
     * The value will be part of the fragment key stored in the parent fragment.
     *
     * @param resource the current rendered resource
     * @param renderContext the current renderContext
     * @param properties the current fragment properties
     * @return the value for this part of the key
     */
    String getValue(Resource resource, RenderContext renderContext, Properties properties);

    /**
     * Replace the keyPart for construct the final key used to store the fragment in cache
     * This function will be call every time to construct the final key and try get fragment from cache, that's why this operation
     * need to be as light as possible.
     * Avoid read JCR, heavy operations or heavy processes in general in this function.
     *
     * @param renderContext the current render context
     * @param keyPart the key part, it's the previous value set by the getValue(...) function
     * @return return the replaced value
     */
    String replacePlaceholders(RenderContext renderContext, String keyPart);

}
