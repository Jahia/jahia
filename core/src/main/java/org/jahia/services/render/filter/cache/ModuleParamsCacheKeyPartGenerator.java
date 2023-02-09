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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
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
import java.util.TreeSet;

/**
 * Cache key part generator that serializes JSON module parameters, if present.
 */
public class ModuleParamsCacheKeyPartGenerator implements CacheKeyPartGenerator, RenderContextTuner {

    private static final Logger logger = LoggerFactory.getLogger(ModuleParamsCacheKeyPartGenerator.class);
    private static final String ENCODED_DOUBLE_AT = "&dblAt;";

    @Override
    public String getKey() {
        return "moduleParams";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        Map<String, Serializable> params = resource.getModuleParams();
        return params.size() == 0 ? StringUtils.EMPTY : encodeString(new OrderedJsonObject(params).toString());
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

    protected static String decodeString(String toBeDecoded) {
        if (toBeDecoded != null && toBeDecoded.indexOf('&') != -1) {
            toBeDecoded = StringUtils.replace(toBeDecoded, ENCODED_DOUBLE_AT, "@@");
            toBeDecoded = StringEscapeUtils.unescapeHtml4(toBeDecoded);
        }
        return toBeDecoded;
    }

    protected static String encodeString(String toBeEncoded) {
        if (toBeEncoded != null) {
            toBeEncoded = StringEscapeUtils.escapeHtml4(toBeEncoded);
            toBeEncoded = StringUtils.replace(toBeEncoded, "@@", ENCODED_DOUBLE_AT);
        }
        return toBeEncoded;
    }

    @Override
    public Object prepareContextForContentGeneration(String value, Resource resource, RenderContext renderContext) {
        if (StringUtils.isNotEmpty(value)) {
            try {
                OrderedJsonObject map = new OrderedJsonObject(decodeString(value));
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

    private static class OrderedJsonObject extends JSONObject {

        public OrderedJsonObject(Map<?, ?> map) {
            super(map);
        }

        public OrderedJsonObject(String source) throws JSONException {
            super(source);
        }

        @Override
        public Iterator<?> keys() {
            TreeSet<Object> keys = new TreeSet<>();
            for (Iterator<?> it = super.keys(); it.hasNext(); ) {
                keys.add(it.next());
            }
            return keys.iterator();
        }
    }
}
