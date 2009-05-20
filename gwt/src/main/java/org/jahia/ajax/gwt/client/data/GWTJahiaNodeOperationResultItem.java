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

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 *
 *
 * User: toto
 * Date: Nov 6, 2008 - 4:53:00 PM
 */
public class GWTJahiaNodeOperationResultItem extends BaseModelData implements Serializable {
    public static final int WARNING = 1;
    public static final int ERROR = 2;

    public static final int VALIDATION = 1;
    public static final int WAI = 2;
    public static final int URL = 3;
    
    public GWTJahiaNodeOperationResultItem() {}

    public GWTJahiaNodeOperationResultItem(int type, String message) {
        setLevel(type);
        setMessage(message);
    }

    public Integer getLevel() {
        return get("level");
    }

    public void setLevel(int level) {
        set("level",Integer.valueOf(level));
    }

    public Integer getType() {
        return get("type");
    }

    public void setType(int type) {
        set("type",Integer.valueOf(type));
    }

    public String getMessage() {
        return get("message");
    }

    public void setMessage(String message) {
        set("message", message);
    }


    public String getUrl() {
        return get("url");
    }

    public void setUrl(String url) {
        set("url", url);
    }

    public String getComment() {
        return get("comment");
    }

    public void setComment(String comment) {
        set("comment", comment);
    }

    public boolean isBlocker() {
        return (Boolean) get("blocker");
    }

    public void setBlocker(boolean blocker) {
        set("blocker", Boolean.valueOf(blocker));
    }
}
