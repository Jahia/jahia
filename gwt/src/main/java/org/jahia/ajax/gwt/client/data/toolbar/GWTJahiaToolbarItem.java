/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
