/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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
