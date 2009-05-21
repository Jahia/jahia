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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.store.ListStore;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSEntry;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 12 ao�t 2008
 * Time: 16:02:59
 */
public class RSSWidget extends ContentPanel {

    private GWTJahiaRSSFeed gwtrssFeed;
    private int feedEntryIndex;
    private String feedURL;
    private int maxEntries;
    private int maxEntriesPerPage;
    private ListView<GWTJahiaRSSEntry> view;
    private DateTimeFormat dateTimeFormat;
    private BasePagingLoader loader;
    private PagingToolBar pagingToolBar;

    /**
     *
     * @param feedEntryIndex
     * @param feedURL
     * @param maxEntries
     * @param maxEntriesPerPage
     */
    public RSSWidget(int feedEntryIndex, String feedURL, int maxEntries, int maxEntriesPerPage) {
        this(new FitLayout(), feedEntryIndex, feedURL, maxEntries, maxEntriesPerPage);
    }

    /**
     *
     * @param layout
     * @param feedEntryIndex
     * @param feedURL
     * @param maxEntries
     * @param maxEntriesPerPage
     */
    public RSSWidget(Layout layout, int feedEntryIndex, String feedURL, int maxEntries, int maxEntriesPerPage) {
        super(layout);
        this.feedEntryIndex = feedEntryIndex;
        this.feedURL = feedURL;
        this.maxEntries = maxEntries;
        this.maxEntriesPerPage = maxEntriesPerPage;
        this.initWidget();
    }

    private void initWidget(){

        this.setHeaderVisible(false);
        this.setBodyBorder(false);
        this.setHeight("100%");
        //this.setScrollMode(Style.Scroll.NONE);
        this.view = new ListView<GWTJahiaRSSEntry>() {
            @Override
            protected GWTJahiaRSSEntry prepareData(GWTJahiaRSSEntry model) {
                DateTimeFormat dateTimeFormat = getDateTimeFormat();
                if (dateTimeFormat == null){
                    dateTimeFormat = DateTimeFormat.getShortDateTimeFormat();
                }
                Date date = model.getUpdatedDate();
                if (date != null){
                    model.set("formattedUpdatedDate",dateTimeFormat.format(date));
                }
                date = model.getPublishedDate();
                if (date != null){
                    model.set("formattedPublishedDate",dateTimeFormat.format(date));
                }
                return model;
            }
        };

        final RpcProxy proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaRSSEntry>>() {
            @Override
            public void load(PagingLoadConfig pageConfig, AsyncCallback<PagingLoadResult<GWTJahiaRSSEntry>> callback) {
                //int offset = pageConfig.getOffset();
                //String sortParameter = pageConfig.getSortInfo().getSortField();
                //boolean isAscending = pageConfig.getSortInfo().getSortDir().equals(Style.SortDir.ASC);
                if(gwtrssFeed != null && gwtrssFeed.getEntries() != null){
                    List<GWTJahiaRSSEntry> entries = new ArrayList<GWTJahiaRSSEntry>();
                    int offset = pageConfig.getOffset();
                    for (int i = offset; i < offset + pageConfig.getLimit(); i++) {
                        if (i < gwtrssFeed.getEntries().size()) {
                            entries.add(gwtrssFeed.getEntries().get(i));
                        } else {
                            break;
                        }
                    }
                    callback.onSuccess(new  BasePagingLoadResult<GWTJahiaRSSEntry>(entries,
                            pageConfig.getOffset(),gwtrssFeed.getEntries().size()));
                    view.refresh();
                } else {
                    callback.onSuccess(new  BasePagingLoadResult<GWTJahiaRSSEntry>(new ArrayList<GWTJahiaRSSEntry>()));
                    view.refresh();
                }
            }
        };

        loader = new BasePagingLoader<PagingLoadConfig, BasePagingLoadResult>(proxy);

        // bottom component
        pagingToolBar = new PagingToolBar(maxEntriesPerPage);
        pagingToolBar.bind(loader);
        this.setBottomComponent(pagingToolBar);
    }

    public DateTimeFormat getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(DateTimeFormat dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
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

    public String getFeedURL() {
        return feedURL;
    }

    public void setFeedUrl(String feedURL) {
        this.feedURL = feedURL;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void execute() {
        gwtrssFeed = null;
        this.removeAll();
        this.pagingToolBar.hide();
        Label label = new Label("loading...");
        label.setHeight("100%");
        add(label);
        JahiaServiceAsync service = JahiaService.App.getInstance();

        GWTJahiaPageContext page = new GWTJahiaPageContext();
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());

        service.loadRssFeed(page,this.feedURL,this.maxEntries,
            new AsyncCallback<GWTJahiaRSSFeed>() {
                public void onSuccess(GWTJahiaRSSFeed o) {
                    gwtrssFeed = o;
                    refreshData(false);
                }
                public void onFailure(Throwable throwable) {
                    Log.error("Can't load rss", throwable);
                    gwtrssFeed = null;
                    refreshData(false);
                }
            });
    }

    /**
     * Refresh feed entries
     */
    public void refreshData(boolean fromServer) {
        if (fromServer) {
            execute();
            return;
        }

        this.removeAll();
        // load first rss entries


        if (gwtrssFeed != null) {
            // refresh header
            if (gwtrssFeed.getTitle() != null) {
                if (gwtrssFeed.getEntries() != null) {
                    this.setTitle("(" + gwtrssFeed.getEntries().size() + ") " + gwtrssFeed.getTitle());
                } else {
                    this.setTitle(gwtrssFeed.getTitle());
                }
            }

            // refresh  view mode values
            ListStore store = new ListStore(loader);
            this.view.setStore(store);
            this.view.setItemSelector("div.gwt-rssEntry-item");
            this.view.setTemplate(getRSSFeedEntryNormalTemplate());
            this.view.setHeight("100%");
            this.add(this.view);
            loader.load();
            this.layout();
            this.pagingToolBar.show();
        } else {
            add(new Label("Error when loading feeds..."));
        }
    }

    public native String getRSSFeedEntryShortTemplate() /*-{
        return ['<tpl for=".">',
        '<div class="gwt-rssEntry-item">',
        '<div class="gwt-rssEntry-title"><a href="{link}">{title}</a></div>',
        '<p class="gwt-rssEntry-author">{authorLabel} : {author}</p>' +
        '<p class="gwt-rssEntry-date">{publishedDateLabel} : {formattedPublishedDate}</p>',
        '<p class="gwt-rssEntry-description">{shortDescription}</p></div>',
        '</tpl>'].join("");
    }-*/;

    public native String getRSSFeedEntryNormalTemplate() /*-{
        return ['<tpl for=".">',
        '<div class="gwt-rssEntry-item">',
        '<div class="gwt-rssEntry-title"><a href="{link}">{title}</a></div>',
        '<p class="gwt-rssEntry-author">{authorLabel} : {author}</p>' +
        '<p class="gwt-rssEntry-date">{publishedDateLabel} : {formattedPublishedDate}</p>',
        '<p class="gwt-rssEntry-description">{description}</p></div>',
        '</tpl>'].join("");
    }-*/;

}