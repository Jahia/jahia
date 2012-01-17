/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
