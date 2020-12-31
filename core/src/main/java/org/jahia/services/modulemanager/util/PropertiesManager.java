/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Helps manipulation of dictionary-like maps, converting structures into flat strings properties
 * Provides implementation for PropertiesValues and PropertiesList
 */
public class PropertiesManager {

    private Map<String, String> props;

    /**
     * Create a properties manager based on existing props
     * @param props the props
     */
    public PropertiesManager(Map<String, String> props) {
        this.props = props;
    }

    public PropertiesValues getValues() {
        return new PropertiesValuesImpl("", '.');
    }

    private class PropertiesValuesImpl implements PropertiesValues {
        private String path;
        private Map<String, String> currentProps;
        private char delimiter;

        public PropertiesValuesImpl(String path, char delimiter) {
            this.path = path;
            this.delimiter = delimiter;
            initProps();
        }

        private void initProps() {
            this.currentProps = path.length() == 0 ?
                    props :
                    props.keySet().stream()
                            .filter(k -> k.startsWith(path + delimiter))
                            .collect(Collectors.toMap(s -> s.substring(path.length() + 1), props::get));
        }

        private String getKey(String name) {
            return path.length() == 0 ? name : (path + delimiter + name);
        }

        public String getPath() {
            return path;
        }

        @Override
        public String getProperty(String name) {
            return currentProps.get(name);
        }

        @Override
        public Boolean getBooleanProperty(String name) {
            return currentProps.containsKey(name) ? Boolean.valueOf(currentProps.get(name)) : null;
        }

        @Override
        public Integer getIntegerProperty(String name) {
            return currentProps.containsKey(name) ? Integer.valueOf(currentProps.get(name)) : null;
        }

        @Override
        public byte[] getBinaryProperty(String name) {
            return currentProps.containsKey(name) ? Base64.getDecoder().decode(currentProps.get(name)) : null;
        }

        @Override
        public void setProperty(String name, String value) {
            props.put(getKey(name), value);
            initProps();
        }

        @Override
        public void setBooleanProperty(String name, boolean value) {
            setProperty(name, Boolean.toString(value));
        }

        @Override
        public void setIntegerProperty(String name, int value) {
            setProperty(name, Integer.toString(value));
        }

        @Override
        public void setBinaryProperty(String name, byte[] data) {
            setProperty(name, Base64.getEncoder().encodeToString(data));
        }

        @Override
        public String removeProperty(String name) {
            return props.remove(getKey(name));
        }

        public PropertiesListImpl getList(String name) {
            String subPath = getKey(name);
            return new PropertiesListImpl(subPath);
        }

        @Override
        public PropertiesValuesImpl getValues(String name) {
            String subPath = getKey(name);
            return new PropertiesValuesImpl(subPath, '.');
        }

        @Override
        public void remove(String name) {
            String k = getKey(name);
            props.keySet().removeAll(props.keySet().stream()
                    .filter(key -> key.equals(k) || key.startsWith(k + '.') || key.startsWith(k + '['))
                    .collect(Collectors.toList())
            );
        }

        @Override
        public Set<String> getKeys() {
            initProps();
            return currentProps.keySet().stream()
                    .flatMap(s -> Arrays.stream(StringUtils.split(s, "[.")).limit(1))
                    .collect(Collectors.toSet());
        }

        @Override
        public JSONObject toJSON() throws JSONException {
            JSONObject response = new JSONObject();
            for (String key : getKeys()) {
                if (getList(key).size > 0) {
                    response.put(key, getList(key).toJSON());
                } else if (!getValues(key).isEmpty()) {
                    response.put(key, getValues(key).toJSON());
                } else {
                    response.put(key, getProperty(key));
                }
            }
            return response;
        }

        public void updateFromJSON(JSONObject object) throws JSONException {
            Iterator<?> it = object.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                Object value = object.get(key);
                if (value instanceof JSONObject) {
                    getValues(key).updateFromJSON((JSONObject)value);
                } else if (value instanceof JSONArray) {
                    getList(key).updateFromJSON((JSONArray)value);
                } else {
                    setProperty(key, value.toString());
                }
            }
        }

        public boolean isEmpty() {
            return currentProps.isEmpty();
        }
    }

    private class PropertiesListImpl implements PropertiesList {
        private PropertiesValuesImpl values;
        private int size;

        public PropertiesListImpl(String path) {
            values = new PropertiesValuesImpl(path, '[');
            size = values.getKeys().stream()
                    .map(k -> Integer.parseInt(k.substring(0, k.indexOf(']'))))
                    .max(Comparator.naturalOrder())
                    .orElse(-1) + 1;
        }

        @Override
        public int getSize() {
            return size;
        }

        private String getKey(int index) {
            if (index >= size) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            return index + "]";
        }

        private String getNextKey() {
            return size + "]";
        }

        public String getProperty(int index) {
            return values.getProperty(getKey(index));
        }

        public void addProperty(String value) {
            values.setProperty(getNextKey(), value);
            size++;
        }

        public void setProperty(int index, String value) {
            values.setProperty(getKey(index), value);
        }

        public Boolean getBooleanProperty(int index) {
            return values.getBooleanProperty(getKey(index));
        }

        public void addBooleanProperty(boolean value) {
            values.setBooleanProperty(getNextKey(), value);
            size++;
        }

        public void setBooleanProperty(int index, boolean value) {
            values.setBooleanProperty(getKey(index), value);
        }

        public Integer getIntegerProperty(int index) {
            return values.getIntegerProperty(getKey(index));
        }

        public void addIntegerProperty(int value) {
            values.setIntegerProperty(getNextKey(), value);
            size++;
        }

        public void setIntegerProperty(int index, int value) {
            values.setIntegerProperty(getKey(index), value);
        }

        public byte[] getBinaryProperty(int index) {
            return values.getBinaryProperty(getKey(index));
        }

        public void addBinaryProperty(byte[] value) {
            values.setBinaryProperty(getNextKey(), value);
            size++;
        }

        public void setBinaryProperty(int index, byte[] value) {
            values.setBinaryProperty(getKey(index), value);
        }

        public PropertiesValuesImpl getValues(int index) {
            return values.getValues(getKey(index));
        }

        public PropertiesValuesImpl addValues() {
            PropertiesValuesImpl v = values.getValues(getNextKey());
            size++;
            return v;
        }

        public PropertiesListImpl getList(int index) {
            return values.getList(getKey(index));
        }

        public PropertiesListImpl addList() {
            PropertiesListImpl v = values.getList(getNextKey());
            size++;
            return v;
        }

        @Override
        public JSONArray toJSON() throws JSONException {
            JSONArray response = new JSONArray();

            for (int i = 0; i < size; i++) {
                if (getList(i).size > 0) {
                    response.put(getList(i).toJSON());
                } else if (!getValues(i).isEmpty()) {
                    response.put(getValues(i).toJSON());
                } else if (getProperty(i) != null) {
                    response.put(getProperty(i));
                } else {
                    response.put((Object) null);
                }
            }

            return response;
        }

        public void updateFromJSON(JSONArray array) throws JSONException {
            int length = array.length();
            for (int index=0; index<length; index++) {
                Object value = array.get(index);
                if (value instanceof JSONObject) {
                    getValues(index).updateFromJSON((JSONObject)value);
                } else if (value instanceof JSONArray) {
                    getList(index).updateFromJSON((JSONArray)value);
                } else {
                    setProperty(index, value.toString());
                }
            }
        }
    }

}
