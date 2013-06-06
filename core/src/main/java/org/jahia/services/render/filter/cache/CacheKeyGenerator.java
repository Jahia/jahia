/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

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
     * Parses the resource path from the provided key.
     * 
     * @param key the cache key to be decomposed
     * @return the resource path, parsed from the provided key
     * @throws ParseException in case of a malformed key
     */
    String getPath(String key);

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
