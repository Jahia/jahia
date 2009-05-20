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
package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 8, 2008
 * Time: 6:35:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowHistoryEntry extends BaseModelData implements Serializable {

    public GWTJahiaWorkflowHistoryEntry() {
    }

    public GWTJahiaWorkflowHistoryEntry(Date date, String action, String user, String comment, String language) {
        setDate(date);
        setAction(action);
        setUser(user);
        setComment(comment);
        setLanguage(language);
    }

    public Date getDate() {
        return get("date") ;
    }

    public void setDate(Date date) {
        set("date",date);
    }

    public String getAction() {
        return get("action") ;
    }

    public void setAction(String action) {
        set("action",action);
    }

    public String getUser() {
        return get("user") ;
    }

    public void setUser(String user) {
        set("user", user);
    }

    public String getComment() {
        return get("comment") ;
    }

    public void setComment(String comment) {
        set("comment", comment);
    }

    public String getLanguage() {
        return get("language") ;
    }

    public void setLanguage(String language) {
        set("language",language);
    }
}
