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

/**
 * User: jahia
 * Date: 3 avr. 2008
 * Time: 15:21:59
 */
public class GWTJahiaProperty implements Serializable {

    private String name;
    private String value;

    public GWTJahiaProperty() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param name
     * @param value
     */
    public GWTJahiaProperty(String name, String value) {
        this();
        this.name = name;
        this.value = value;
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
}
