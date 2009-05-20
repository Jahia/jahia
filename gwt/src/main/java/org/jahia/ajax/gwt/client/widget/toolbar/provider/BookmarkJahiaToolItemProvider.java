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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import org.jahia.ajax.gwt.client.data.GWTJahiaBookmark;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;

import java.util.List;

/**
 * User: jahia
 * Date: 31 juil. 2008
 * Time: 15:24:31
 */
public class BookmarkJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    /**
     * Create a new toolItem
     *
     * @param gwtToolbarItem
     * @return
     */
    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

    /**
     * Executed when the item is clicked
     *
     * @param gwtToolbarItem
     * @return
     */
    public SelectionListener<ComponentEvent> getSelectListener(GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    /**
     * Create a ContentPanel
     *
     * @param gwtToolbarItem
     * @return
     */
    public Widget createWidget(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        Log.debug(" create bookwidget as a frame");
        final DataList dataList = new DataList();

        // load after rendering
        DeferredCommand.addCommand(new BookmarkLoaderCommand(dataList));

        return dataList;
    }

    /**
     * Loader for bookmarks
     */
    private class BookmarkLoaderCommand implements Command {
        private DataList list;

        private BookmarkLoaderCommand(DataList list) {
            this.list = list;
        }

        public void execute() {
            JahiaService.App.getInstance().getBookmarks(JahiaPageEntryPoint.getJahiaGWTPage(), new AsyncCallback() {
                public void onSuccess(Object o) {
                    List<GWTJahiaBookmark> gwtJahiaBookwarks = (List) o;
                    Log.debug("BookmarkLoaderCommand - Found " + gwtJahiaBookwarks.size() + " bookmarks.");
                    for (GWTJahiaBookmark bookmark : gwtJahiaBookwarks) {
                        DataListItem item = new DataListItem();
                        item.setText(bookmark.getTitle());
                        list.add(item);
                    }
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Enable to load bookmark. ", throwable);
                }
            });
        }
    }
}
