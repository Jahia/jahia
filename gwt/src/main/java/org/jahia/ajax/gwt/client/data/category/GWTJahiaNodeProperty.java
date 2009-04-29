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
package org.jahia.ajax.gwt.client.data.category;

import java.io.Serializable;


/**
 * This is a bean to wrap a property.
 */
public class GWTJahiaNodeProperty implements Serializable {
    private String name;
    private String value;
    private boolean readOnly;

    public GWTJahiaNodeProperty() {
    }

    public GWTJahiaNodeProperty cloneObject() {
        GWTJahiaNodeProperty prop = new GWTJahiaNodeProperty();
        prop.setName(getName());
        prop.setValue(getValue());
        return prop;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof GWTJahiaNodeProperty)) {
            return false;
        }

        // compare name
        GWTJahiaNodeProperty that = (GWTJahiaNodeProperty) o;
        if (name == null && that.name == null) {
            return true;
        }

        if (name != null) {
            if (that.name != null) {
                return name.equalsIgnoreCase(that.name);
            } else {
                return false;
            }
        } else {
            if (that.name == null) {
                return true;
            } else {
                return false;
            }
        }
    }


}
