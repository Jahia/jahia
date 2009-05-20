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
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 29 ao�t 2008
 * Time: 10:56:17
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaObjectKey implements Serializable {

    static final String KEY_SEPARATOR = "_";

    private String key;
    private String type;
    private int idInType;

    public GWTJahiaObjectKey(String type,String idInType) {
        StringBuffer buf = new StringBuffer(50);
        buf.append(type);
        buf.append(KEY_SEPARATOR);
        buf.append(idInType);
        this.key = buf.toString();
        try {
            this.idInType = Integer.parseInt(idInType);
        } catch (NumberFormatException e) {
            this.idInType = -1;
        }
    }

    public GWTJahiaObjectKey(String type,int idInType) {
        StringBuffer buf = new StringBuffer(50);
        buf.append(type);
        buf.append(KEY_SEPARATOR);
        buf.append(idInType);
        this.key = buf.toString();
        this.idInType = idInType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIdInType() {
        return idInType;
    }

    public void setIdInType(int idInType) {
        this.idInType = idInType;
    }
}
