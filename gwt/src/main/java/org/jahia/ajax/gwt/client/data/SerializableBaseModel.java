package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.ModelData;

import java.io.Serializable;
import java.util.*;

/**
 * Simple implementation of ModelData which can be serialized
 */
public class SerializableBaseModel implements ModelData, Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, String> strings;
    private Map<String, Integer> integers;
    private Map<String, Boolean> booleans;

    private transient Map<String, Serializable> properties;

    public SerializableBaseModel() {
    }

    public <X> X get(String property) {
        if (strings != null && strings.containsKey(property)) {
            return (X) strings.get(property);
        }
        if (integers != null && integers.containsKey(property)) {
            return (X) integers.get(property);
        }
        if (booleans != null && booleans.containsKey(property)) {
            return (X) booleans.get(property);
        }
        if (properties != null) {
            return (X) properties.get(property);
        }
        return null;
    }


    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Serializable>();
        }
        Map<String, Object> returning = new HashMap<String, Object>();
        for (Map.Entry<String, Serializable> entry : properties.entrySet()) {
            returning.put(entry.getKey(), entry.getValue());
        }
        if (strings != null) {
            for (Map.Entry<String, String> entry : strings.entrySet()) {
                returning.put(entry.getKey(), entry.getValue());
            }
        }
        if (integers != null) {
            for (Map.Entry<String, Integer> entry : integers.entrySet()) {
                returning.put(entry.getKey(), entry.getValue());
            }
        }
        if (booleans != null) {
            for (Map.Entry<String, Boolean> entry : booleans.entrySet()) {
                returning.put(entry.getKey(), entry.getValue());
            }
        }
        return returning;
    }


    public Collection<String> getPropertyNames() {
        if (properties == null) {
            properties = new HashMap<String, Serializable>();
        }
        Collection<String> returning = new HashSet<String>();
        returning.addAll(properties.keySet());
        if (strings != null) {
            returning.addAll(strings.keySet());
        }
        if (integers != null) {
            returning.addAll(integers.keySet());
        }
        if (booleans != null) {
            returning.addAll(booleans.keySet());
        }

        return returning;
    }


    public <X> X remove(String property) {
        if (strings != null && strings.containsKey(property)) {
            return (X) strings.remove(property);
        }
        if (integers != null && integers.containsKey(property)) {
            return (X) integers.remove(property);
        }
        if (booleans != null && booleans.containsKey(property)) {
            return (X) booleans.remove(property);
        }
        if (properties != null) {
            return (X) properties.remove(property);
        }
        return null;
    }


    public <X> X set(String property, X value) {
        if (value instanceof String) {
            if (strings == null) {
                strings = new LinkedHashMap<String, String>();
            }
            return (X) strings.put(property, (String) value);
        } else if (value instanceof Integer) {
            if (integers == null) {
                integers = new LinkedHashMap<String, Integer>();
            }
            return (X) integers.put(property, (Integer) value);
        } else if (value instanceof Boolean) {
            if (booleans == null) {
                booleans = new LinkedHashMap<String, Boolean>();
            }
            return (X) booleans.put(property, (Boolean) value);
        }
        if (properties == null) {
            properties = new LinkedHashMap<String, Serializable>();
        }
        return (X) properties.put(property, (Serializable) value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SerializableBaseModel)) {
            return false;
        }

        SerializableBaseModel that = (SerializableBaseModel) o;

        if (booleans != null ? !booleans.equals(that.booleans) : that.booleans != null) {
            return false;
        }
        if (integers != null ? !integers.equals(that.integers) : that.integers != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        if (strings != null ? !strings.equals(that.strings) : that.strings != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = strings != null ? strings.hashCode() : 0;
        result = 31 * result + (integers != null ? integers.hashCode() : 0);
        result = 31 * result + (booleans != null ? booleans.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}

