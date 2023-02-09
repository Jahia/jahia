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
     *
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

        public Map<String, Object> getStructuredMap() {
            Map<String, Object> map = new HashMap<>();
            for (String key : getKeys()) {
                if (getList(key).size > 0) {
                    map.put(key, getList(key).getStructuredList());
                } else if (!getValues(key).isEmpty()) {
                    map.put(key, getValues(key).getStructuredMap());
                } else {
                    map.put(key, getProperty(key));
                }
            }
            return Collections.unmodifiableMap(map);
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
                    getValues(key).updateFromJSON((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    getList(key).updateFromJSON((JSONArray) value);
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

        public List<Object> getStructuredList() {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (getList(i).size > 0) {
                    list.add(getList(i).getStructuredList());
                } else if (!getValues(i).isEmpty()) {
                    list.add(getValues(i).getStructuredMap());
                } else if (getProperty(i) != null) {
                    list.add(getProperty(i));
                } else {
                    list.add(null);
                }
            }
            return Collections.unmodifiableList(list);
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
            for (int index = 0; index < length; index++) {
                Object value = array.get(index);
                if (value instanceof JSONObject) {
                    if (size == index) {
                        addValues();
                    }
                    getValues(index).updateFromJSON((JSONObject) value);
                } else if (value instanceof JSONArray) {
                    if (size == index) {
                        addList();
                    }
                    getList(index).updateFromJSON((JSONArray) value);
                } else {
                    if (size == index) {
                        addValues();
                    }
                    setProperty(index, value.toString());
                }
            }
        }
    }

}
