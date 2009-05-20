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
package org.jahia.services.workflow;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 8, 2008
 * Time: 6:47:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalWorkflowHistoryEntry {
    private Date date;
    private String action;
    private String user;
    private String comment;
    private String language;

    public ExternalWorkflowHistoryEntry(Date date, String action, String user, String comment, String language) {
        this.date = date;
        this.action = action;
        this.user = user;
        this.comment = comment;
        this.language = language;
    }

    public Date getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }

    public String getUser() {
        return user;
    }

    public String getComment() {
        return comment;
    }

    public String getLanguage() {
        return language;
    }
}
