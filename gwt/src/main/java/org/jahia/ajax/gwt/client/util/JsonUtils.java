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
    protected static String encodeValue(Object value) {
        if (value instanceof Date) {
            return "" + ((Date) value).getTime();
        } else if (value instanceof Integer) {
            return "" + value;
        } else if (value instanceof Float) {
            return "" + value;
        }
        return "" + value.toString();
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
            } else if (val instanceof JsonSerializable) {
                jsona.set(i, serialize(((JsonSerializable) val).getDataForJsonSerialization()));
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
            } else if (val instanceof JsonSerializable) {
                jsobj.put(key, serialize(((JsonSerializable) val).getDataForJsonSerialization()));
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
