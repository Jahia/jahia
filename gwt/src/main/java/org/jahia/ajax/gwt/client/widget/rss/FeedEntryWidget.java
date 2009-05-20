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
package org.jahia.ajax.gwt.client.widget.rss;

import com.google.gwt.user.client.ui.HTML;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import java.util.List;

import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSEntry;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSContent;

/**
 * User: jahia
 * Date: 14 ao�t 2008
 * Time: 10:57:13
 */
public class FeedEntryWidget extends ContentPanel {
    public FeedEntryWidget(GWTJahiaRSSEntry entry) {
        setHeaderVisible(false);
        setBodyBorder(false);
        setBorders(false);
        init(entry);
    }

    private void init(GWTJahiaRSSEntry entry) {
        setWidth("100%");

        add(new HTML("<a href='" + entry.getLink() + "'>" + entry.getTitle() + "</a>"));
        String description = entry.getDescription();
        String content = "";
        if (description != null) {
            content = description;
        } else {
            List<GWTJahiaRSSContent> contentList = entry.getContents();
            if (contentList != null && !contentList.isEmpty()) {
                content = contentList.get(0).getValue();
            }
        }
        Log.debug("RSS content: "+content);
        add(new HTML(content));
        layout();
    }
}