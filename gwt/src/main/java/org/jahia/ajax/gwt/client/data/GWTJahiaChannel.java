package org.jahia.ajax.gwt.client.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public GWTJahiaChannel(String value, String display, String image, Map<String,String> capabilities) {
        super(value, display);
        set("image", image);
        setCapabilities(capabilities);
    }

    public Map<String,String> getCapabilities() {
        return get("capabilities");
    }

    public void setCapabilities(Map<String,String> capabilities) {
        set("capabilities", capabilities);
    }

    public String getCapability(String capabilityName) {
        if (capabilityName == null) {
            return  null;
        }
        if (getCapabilities() == null) {
            return null;
        }
        return getCapabilities().get(capabilityName);
    }

    public List<String> getVariants() {
        String variantListStr = getCapability("variants");
        if (variantListStr == null) {
            return new ArrayList<String>();
        }
        String[] variantArray = variantListStr.split(",");
        return Arrays.asList(variantArray);
    }

    public String getCapabilityListAtIndex(String capabilityName, int listIndex) {
        String capabilityListValueStr = getCapability(capabilityName);
        if (capabilityListValueStr == null) {
            return null;
        }
        String[] capabilityValueArray = capabilityListValueStr.split(",");
        if (listIndex >=0 && listIndex < capabilityValueArray.length) {
            return capabilityValueArray[listIndex];
        } else {
            return null;
        }
    }

    public int[] getResolutionFromString(String resolutionString) {
        if (resolutionString == null) {
            return new int[0];
        }
        String[] resolutionStrArray = resolutionString.split("x");
        if (resolutionStrArray == null || resolutionStrArray.length != 2) {
            return new int[0];
        }
        int[] result = new int[2];
        result[0] = Integer.parseInt(resolutionStrArray[0]);
        result[1] = Integer.parseInt(resolutionStrArray[1]);
        return result;
    }

    public int[] getResolutionCapability(String capabilityName) {
        String resolutionCapabilityStr = getCapability(capabilityName);
        if (resolutionCapabilityStr == null) {
            return new int[0];
        }
        return getResolutionFromString(resolutionCapabilityStr);
    }

    public int[] getResolutionCapabilityAtIndex(String capabilityName, int variantIndex) {
        String resolutionCapabilityStr = getCapabilityListAtIndex(capabilityName, variantIndex);
        return getResolutionFromString(resolutionCapabilityStr);
    }

    public String getVariantDisplayName(int variantIndex) {
        return getCapabilityListAtIndex("variants-displayNames", variantIndex);
    }

    public String getVariantDecoratorImage(int variantIndex) {
        return getCapabilityListAtIndex("decorator-images", variantIndex);
    }

    public int[] getVariantDecoratorImageSize(int variantIndex) {
        return getResolutionCapabilityAtIndex("decorator-image-sizes", variantIndex);
    }

    public int[] getVariantDecoratorScreenPosition(int variantIndex) {
        return getResolutionCapabilityAtIndex("decorator-screen-positions", variantIndex);
    }

    public int[] getVariantUsableResolution(int variantIndex) {
        return getResolutionCapabilityAtIndex("usable-resolutions", variantIndex);
    }

}
