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
 package org.jahia.hibernate.listener;

import org.hibernate.event.PostInsertEvent;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 2 sept. 2005
 * Time: 14:19:16
 * To change this template use File | Settings | File Templates.
 */
public class HibPostInsertEventListener implements org.hibernate.event.PostInsertEventListener {

    private org.hibernate.event.PostInsertEventListener postInsertEventListener;

    public HibPostInsertEventListener(org.hibernate.event.PostInsertEventListener
                                        postInsertEventListener) {
        this.postInsertEventListener = postInsertEventListener;
    }

    public void onPostInsert(final PostInsertEvent postInsertEvent) {

        if (this.postInsertEventListener != null) {
            this.postInsertEventListener.onPostInsert(postInsertEvent);
        }
    }
}
