/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
