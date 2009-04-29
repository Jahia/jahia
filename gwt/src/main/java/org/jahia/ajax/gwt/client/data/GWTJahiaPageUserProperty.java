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
 * Created by Jahia.
 * User: ktlili
 * Date: 22 nov. 2007
 * Time: 15:11:16
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPageUserProperty implements Serializable {
    private int pageId = -1;
    private String principalKey;
    private String principalType;
    private String propType;
    private String name;
    private String value;

    public GWTJahiaPageUserProperty() {

    }

    public GWTJahiaPageUserProperty(int pageId, String principalKey,String principalType,String propType, String name, String value) {
        this.pageId = pageId;
        this.principalKey = principalKey ;
        this.principalType = principalType;
        this.propType = propType;
        this.name = name;
        this.value = value;
    }


    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }


    public String getPrincipalKey() {
        return principalKey;
    }

    public void setPrincipalKey(String principalKey) {
        this.principalKey = principalKey;
    }

    public String getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }

    public String getPropType() {
        return propType;
    }

    public void setPropType(String propType) {
        this.propType = propType;
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
