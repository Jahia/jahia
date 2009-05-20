/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data;


import java.io.Serializable;
import java.util.List;

/**
 * User: ktlili
 * Date: 4 sept. 2008
 * Time: 18:40:06
 */
public class GWTJahiaUserProperty implements Serializable {
    private final static String[] jahiaUserProperties = {"lastname", "firstname", "organization", "email", "emailNotificationsDisabled", "preferredLanguage"};
    public static String CUSTOM_USER_PROPERTY_PREFIX = "mysettings-user-property-#";

    private String label;
    private boolean isPassword;
    private boolean isReadOnly;
    private String key;
    private String display;
    private List<GWTJahiaBasicDataBean> values;
    private GWTJahiaBasicDataBean value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isPassword() {
        return isPassword;
    }

    public void setPassword(boolean password) {
        isPassword = password;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public String getKey() {
        return key;
    }

    public String getRealKey() {
        if (key == null) {
            return null;
        }
        if (isJahiaMySettingsProperty()) {
            return key;
        }
        return CUSTOM_USER_PROPERTY_PREFIX + key;
    }

    public void setRealKey(String realKey) {
        if (realKey == null) {
            key = null;
        } else {
            setKey(realKey.replaceAll(CUSTOM_USER_PROPERTY_PREFIX, ""));
        }
    }

    public boolean isJahiaMySettingsProperty() {
        return getJahiaMySettingsPropertyIndex() > -1;
    }

    public int getJahiaMySettingsPropertyIndex() {
        if (key != null) {
            int index = 0;
            for (String jahiaProperty : jahiaUserProperties) {
                if (key.equalsIgnoreCase(jahiaProperty)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public List<GWTJahiaBasicDataBean> getValues() {
        return values;
    }

    public void setValues(List<GWTJahiaBasicDataBean> values) {
        this.values = values;
    }

    public GWTJahiaBasicDataBean getValue() {
        return value;
    }

    public void setValue(GWTJahiaBasicDataBean value) {
        this.value = value;
    }
}
