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
package org.jahia.ajax.gwt.client.data.mysettings;

import java.io.Serializable;

/**
 * User: ktlili
 * Date: 4 sept. 2008
 * Time: 18:15:14
 */
public class GWTJahiaMySetting implements Serializable {
    private String name;
    private String value;
    private int storage;

    public GWTJahiaMySetting() {
        storage = 1;
    }


    public GWTJahiaMySetting(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }


    public GWTJahiaMySetting(String name, String value, int storage) {
        this.name = name;
        this.value = value;
        this.storage = storage;
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

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }
}
