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
 package org.jahia.services.search;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 f�vr. 2005
 * Time: 12:49:37
 * To change this template use File | Settings | File Templates.
 */
public class SearchEvent extends EventObject {

    private static final long serialVersionUID = -4533775679684393425L;
    private long eventTime;
    private Object data;

    /***
     * constructor
     * composes automatically the time when the event was triggered
     *
     * @param source the object that generated this event
     * @param data
     */
    public SearchEvent (Object source,
                        Object data) {
        super(source);
        this.data = data;
    }

    public long getEventTime() {
        return eventTime;
    }

    public Object getData() {
        return this.data;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

}
