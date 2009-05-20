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

import org.jahia.data.events.JahiaEvent;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.content.ContentObject;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 f�vr. 2004
 * Time: 18:41:45
 * To change this template use Options | File Templates.
 */
public class WorkflowEvent extends JahiaEvent {
    private static final long serialVersionUID = 324359523025300668L;
    private JahiaUser user;
    private String languageCode;
    private boolean shouldDescend;
    private boolean isNew = false;

    public WorkflowEvent(Object source, ContentObject theObj,
                         JahiaUser user, String languageCode, boolean shouldDescend) {
        super(source, null, theObj);
        this.user = user;
        this.languageCode = languageCode;
        this.shouldDescend = shouldDescend;
    }

    public JahiaUser getUser() {
        return user;
    }

    public void setUser(JahiaUser user) {
        this.user = user;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public boolean isShouldDescend() {
        return shouldDescend;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}

