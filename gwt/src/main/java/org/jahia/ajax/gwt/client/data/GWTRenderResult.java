package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * GWT bean used to encapsulate result of a rendering call.
 *
 */
public class GWTRenderResult implements Serializable {
    private String result;
    private Map<String,Set<String>> staticAssets;

    public GWTRenderResult() {
    }

    public GWTRenderResult(String result, Map<String, Set<String>> staticAssets) {
        this.result = result;
        this.staticAssets = staticAssets;
    }

    public String getResult() {
        return result;
    }

    public Map<String, Set<String>> getStaticAssets() {
        return staticAssets;
    }
}
