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
 package org.jahia.content.events;

import org.jahia.content.ContentObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.params.ProcessingContext;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class ContentUndoStagingEvent extends JahiaEvent {

    private int siteId;

    public ContentUndoStagingEvent(Object source,
                                   int siteId,
                                   ProcessingContext jParams) {
        super(source, jParams, source);
        this.siteId = siteId;
    }

    public int getSiteId() {
        return siteId;
    }

    public ContentObject getContentObject() {
        return (ContentObject)getSource();
    }

}
