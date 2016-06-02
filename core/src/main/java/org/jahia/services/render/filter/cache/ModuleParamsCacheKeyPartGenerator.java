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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Cache key part generator that serializes JSON module parameters, if present.
 */
public class ModuleParamsCacheKeyPartGenerator implements CacheKeyPartGenerator, RenderContextTuner {

    private static final Logger logger = LoggerFactory.getLogger(ModuleParamsCacheKeyPartGenerator.class);

    @Override
    public String getKey() {
        return "moduleParams";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        Map<String, Serializable> params = resource.getModuleParams();
        return params.size() == 0 ? StringUtils.EMPTY : encodeString(new JSONObject(params).toString());
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

    private String encodeString(String toBeEncoded) {
        return toBeEncoded != null ? toBeEncoded.replace("#", "#1").replace("@@", "#2") : null;
    }

    private String decodeString(String toBeDecoded) {
        return toBeDecoded != null && toBeDecoded.contains("#") ? toBeDecoded.replace("#2", "@@").replace("#1", "#") : toBeDecoded;
    }

    @Override
    public Object prepareContextForContentGeneration(String value, Resource resource, RenderContext renderContext) {
        if (StringUtils.isNotEmpty(value)) {
            try {
                JSONObject map = new JSONObject(decodeString(value));
                Iterator<?> keys = map.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    resource.getModuleParams().put(key, (Serializable) map.get(key));
                }
            } catch (JSONException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void restoreContextAfterContentGeneration(String value, Resource resource, RenderContext renderContext, Object original) {
    }
}
