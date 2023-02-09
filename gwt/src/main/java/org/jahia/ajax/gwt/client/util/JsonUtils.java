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
package org.jahia.ajax.gwt.client.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Utility class to serialize object into {@link JSONObject}.
 *
 * @author Sergiy Shyrkov
 *
 */
public class JsonUtils {

    private JsonUtils() {
    }

    private static String encodeValue(Object value) {
        if (value instanceof Date) {
            return Long.toString(((Date) value).getTime());
        }
        return value.toString();
    }

    /**
     * Returns a JSON representation for the specified data list.
     *
     * @param data the data to be serialized
     * @return a {@link JSONObject}, representing the supplied data
     */
    @SuppressWarnings("unchecked")
    public static JSONArray serialize(List<Object> data) {
        JSONArray jsona = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            Object val = data.get(i);
            if (val instanceof Map) {
                jsona.set(i, serialize((Map<String, Object>) val));
            } else if (val instanceof List) {
                jsona.set(i, serialize((List<Object>) val));
            } else if (val instanceof String) {
                jsona.set(i, new JSONString(encodeValue(val)));
            } else if (val instanceof Number) {
                jsona.set(i, new JSONString(encodeValue(val)));
            } else if (val instanceof Boolean) {
                jsona.set(i, JSONBoolean.getInstance((Boolean) val));
            } else if (val == null) {
                jsona.set(i, JSONNull.getInstance());
            } else if (val instanceof Date) {
                jsona.set(i, new JSONString(encodeValue(val)));
            } else if (val instanceof ModelData) {
                jsona.set(i, serialize(((ModelData) val).getProperties()));
            } else if (val instanceof EventDataSupplier) {
                jsona.set(i, serialize(((EventDataSupplier) val).getEventData()));
            }
        }
        return jsona;
    }

    /**
     * Returns a JSON representation for the specified data map.
     *
     * @param data the data to be serialized
     * @return a {@link JSONObject}, representing the supplied data
     */
    @SuppressWarnings("unchecked")
    public static JSONObject serialize(Map<String, Object> data) {
        JSONObject jsobj = new JSONObject();
        for (String key : data.keySet()) {
            Object val = data.get(key);
            if (val instanceof String) {
                jsobj.put(key, new JSONString(encodeValue(val)));
            } else if (val instanceof Date) {
                jsobj.put(key, new JSONString(encodeValue(val)));
            } else if (val instanceof Number) {
                jsobj.put(key, new JSONString(encodeValue(val)));
            } else if (val instanceof Boolean) {
                jsobj.put(key, JSONBoolean.getInstance((Boolean) val));
            } else if (val == null) {
                jsobj.put(key, JSONNull.getInstance());
            } else if (val instanceof Map) {
                jsobj.put(key, serialize((Map<String, Object>) val));
            } else if (val instanceof List) {
                jsobj.put(key, serialize((List<Object>) val));
            } else if (val instanceof ModelData) {
                jsobj.put(key, serialize(((ModelData) val).getProperties()));
            } else if (val instanceof EventDataSupplier) {
                jsobj.put(key, serialize(((EventDataSupplier) val).getEventData()));
            }
        }

        return jsobj;
    }

    /**
     * Returns a JSON representation for the specified model data.
     *
     * @param data the data to be serialized
     * @return a {@link JSONObject}, representing the supplied data
     */
    public static JSONObject serialize(ModelData data) {
        return serialize(data.getProperties());
    }
}
