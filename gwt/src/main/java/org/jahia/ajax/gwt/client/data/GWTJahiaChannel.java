package org.jahia.ajax.gwt.client.data;

import java.util.Map;

/**
 * GWT Bean to access channel information
 */
public class GWTJahiaChannel extends GWTJahiaValueDisplayBean {

    public GWTJahiaChannel() {

    }

    public GWTJahiaChannel(String value, String display) {
        super(value, display);
    }

    public GWTJahiaChannel(String value, String display, Map<String,String> capabilities) {
        super(value, display);
        setCapabilities(capabilities);
    }

    public Map<String,String> getCapabilities() {
        return get("capabilities");
    }

    public void setCapabilities(Map<String,String> capabilities) {
        set("capabilities", capabilities);
    }

}
