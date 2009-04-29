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
package org.jahia.ajax.gwt.client.data.toolbar;

import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:41:31
 */
public class GWTJahiaToolbarItem implements Serializable {
    private String id;
    private String type;
    private String mediumIconStyle;
    private String minIconStyle;
    private String title;
    private boolean displayTitle;
    private String description;
    private boolean disabled;
    private boolean selected;
    private Map<String, GWTJahiaProperty> properties = new HashMap<String, GWTJahiaProperty>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMinIconStyle() {
        return minIconStyle;
    }

    public void setMinIconStyle(String minIconStyle) {
        this.minIconStyle = minIconStyle;
    }

    public String getMediumIconStyle() {
        return mediumIconStyle;
    }

    public void setMediumIconStyle(String mediumIconStyle) {
        this.mediumIconStyle = mediumIconStyle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Map<String, GWTJahiaProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, GWTJahiaProperty> properties) {
        this.properties = properties;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void addProperty(GWTJahiaProperty gwtProperty) {
        if (gwtProperty != null) {
            properties.put(gwtProperty.getName(), gwtProperty);
        }
    }
}
