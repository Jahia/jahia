package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;
import java.util.Map;

/**
 * A single entry of a static asset
 */
public class GWTStaticAssetEntry implements Serializable {

    private String key;
    private Map<String,String> options;

    public GWTStaticAssetEntry() {
    }

    public GWTStaticAssetEntry(String key, Map<String, String> options) {
        this.key = key;
        this.options = options;
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
}
