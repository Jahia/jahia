/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.rss;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSEntry;
import org.jahia.ajax.gwt.client.service.JahiaService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 4:29:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSPanel extends ContentPanel {
    private GWTJahiaRSSFeed gwtrssFeed;
    private TextField<String> urlTextField = new TextField<String>();
    private NumberField nbEntriesField = new NumberField();
    private int feedEntryIndex;
    private GWTJahiaPageContext page;

    /**
     * Create an RSS widget in edit mode
     */
    public RSSPanel() {
        super();
        setBodyBorder(false);
        setBorders(false);
        createUI();
    }

    public RSSPanel(GWTJahiaPageContext page, String url, int nbEntries) {
        super();
        setBodyBorder(false);
        setBorders(false);
        this.page = page;
        urlTextField.setValue(url);
        nbEntriesField.setValue(nbEntries);
        createUI();
        execute();
    }


    public void createUI() {
        setHeaderVisible(false);
        setLayout(new RowLayout());
    }


    public int getFeedEntryIndex() {
        return feedEntryIndex;
    }

    public void setFeedEntryIndex(int feedEntryIndex) {
        this.feedEntryIndex = feedEntryIndex;
    }

    public int getEntriesPerPage() {
        if (gwtrssFeed != null) {
            return gwtrssFeed.getNbDisplayedEntries();
        } else {
            return 5;
        }
    }

    public GWTJahiaRSSFeed getGwtrssFeed() {
        return gwtrssFeed;
    }

    public void setGwtrssFeed(GWTJahiaRSSFeed gwtrssFeed) {
        this.gwtrssFeed = gwtrssFeed;
    }

    public void execute() {
        gwtrssFeed = null;
        removeAll();
        add(new Label("loading..."));
        JahiaService.App.getInstance().loadRssFeed(this.page, (String) urlTextField.getValue(), (Integer) nbEntriesField.getValue(), new RssLoadAsyncCallback(this));
    }


    /**
     * Refresh feed entries
     */
    public void refreshData(boolean fromServer) {
        if (fromServer) {
            execute();
            return;
        }

        removeAll();
        // load first rss entries

        if (gwtrssFeed != null) {
            // refresh header

            // refresh edit mode vlaues
            urlTextField.setValue(gwtrssFeed.getUrl());
            nbEntriesField.setValue(gwtrssFeed.getNbDisplayedEntries());

            // refresh  view mode values
            if (gwtrssFeed.getEntries() != null) {
                int row = 0;
                for (int i = getFeedEntryIndex(); (i < gwtrssFeed.getEntries().size() && row < getEntriesPerPage()); i++) {
                    GWTJahiaRSSEntry entry = gwtrssFeed.getEntries().get(i);
                    FeedEntryWidget feedEntryWidget = new FeedEntryWidget(entry);
                    add(feedEntryWidget);
                    if (row % 2 == 0) {
                        feedEntryWidget.addStyleName("gwt-rss-even");
                    } else {
                        add(feedEntryWidget);
                        feedEntryWidget.addStyleName("gwt-rss-odd");
                    }
                    row++;
                }
                HorizontalPanel buttonPanel = createPreviousNextPanel();
                add(buttonPanel);
                //viewWidget.setCellWidth(buttonPanel, "100%");
            } else {
                if (gwtrssFeed.getUrl() != null) {
                    add(new Label("Feed[" + gwtrssFeed.getUrl() + "] is empty"));
                } else {
                    add(new Label("Please set an url (in edit mode)."));
                }
            }
        } else {
            add(new Label("Error when loading feeds..."));
        }
        layout();
    }

    /**
     * Create previous/next panel
     *
     * @return
     */
    private HorizontalPanel createPreviousNextPanel() {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setWidth("100%");

        // previous
        if (getFeedEntryIndex() > 0) {
            Label previous = new Label("");
            previous.addStyleName("gwt-rss-previous");
            previous.addClickListener(new ClickListener() {
                public void onClick(Widget widget) {
                    setFeedEntryIndex(getFeedEntryIndex() - getEntriesPerPage());
                    refreshData(false);
                }
            });
            buttonPanel.add(previous);
            buttonPanel.setCellHorizontalAlignment(previous, HorizontalPanel.ALIGN_LEFT);
        }
        // next
        if (getFeedEntryIndex() < (gwtrssFeed.getEntries().size() - getEntriesPerPage())) {
            Label next = new Label("");
            next.addStyleName("gwt-rss-next");
            next.addClickListener(new ClickListener() {
                public void onClick(Widget widget) {
                    setFeedEntryIndex(getFeedEntryIndex() + getEntriesPerPage());
                    refreshData(false);
                }
            });
            buttonPanel.add(next);
            buttonPanel.setCellHorizontalAlignment(next, HorizontalPanel.ALIGN_RIGHT);
        }
        return buttonPanel;
    }

    /**
     * Load RSS entries
     */
    private class RssLoadAsyncCallback implements AsyncCallback<GWTJahiaRSSFeed> {
        private RSSPanel rssWidget;

        private RssLoadAsyncCallback(RSSPanel rssWidget) {
            this.rssWidget = rssWidget;
        }

        public void onSuccess(GWTJahiaRSSFeed o) {
            // update title
            rssWidget.setGwtrssFeed(o);
            rssWidget.refreshData(false);
        }


        public void onFailure(Throwable throwable) {
            Log.error("Can't load rss", throwable);
            rssWidget.setGwtrssFeed(null);
            rssWidget.refreshData(false);
        }
    }

}
