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
 package org.jahia.services.usermanager;

import java.io.Serializable;

public class UserProperty implements Serializable {

    public static final String CHECKBOX = "checkbox";
    public static final String SELECT_BOX = "selectbox";
    public static final String TEXT_FIELD = "textfield";

    private String name;
    private String value;
    private boolean readOnly;
    private String display;

    public UserProperty(String name, String value, boolean readOnly) {
        this.name = name;
        this.value = value;
        this.readOnly = readOnly;
        this.display = TEXT_FIELD;
    }

    public UserProperty(String name, String value, boolean readOnly, String display) {
        this.name = name;
        this.value = value;
        this.readOnly = readOnly;
        this.display = display;
    }
    
    protected UserProperty(UserProperty copy) {
        this.name = copy.name;
        this.value = copy.value;
        this.readOnly = copy.readOnly;
        this.display = copy.display;
    }
    
    public Object clone() {
        return new UserProperty(this);
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
