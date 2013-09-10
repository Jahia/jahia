package org.jahia.ajax.gwt.client.data;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Element;

import java.io.Serializable;
import java.util.Map;

/**
 * A single entry of a static asset
 */
public class GWTStaticAssetEntry implements Serializable {
    private String id;
    private String key;
    private Map<String,String> options;
    private transient Node element;

    public GWTStaticAssetEntry() {
    }

    public GWTStaticAssetEntry(String key, Map<String, String> options) {
        this.key = key;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Node getElement() {
        return element;
    }

    public void setNode(Node element) {
        this.element = element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTStaticAssetEntry that = (GWTStaticAssetEntry) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
