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
package org.jahia.ajax.gwt.client.widget.sitemap;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.GWTJahiaPageWrapper;
import org.jahia.ajax.gwt.client.service.JahiaContentLegacyService;
import org.jahia.ajax.gwt.client.service.JahiaContentLegacyServiceAsync;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelStringProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 24, 2008
 * Time: 4:22:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SitemapTree extends Tree {

    private TreeLoader<GWTJahiaPageWrapper> loader ;
    private TreeStore<GWTJahiaPageWrapper> store ;
    private TreeBinder<GWTJahiaPageWrapper> binder ;

    private TreeItem lastSelection = null ;

    public SitemapTree(final GWTJahiaPageContext page) {
        final JahiaContentLegacyServiceAsync service = JahiaContentLegacyService.App.getInstance() ;

        // data proxy
        RpcProxy<GWTJahiaPageWrapper, List<GWTJahiaPageWrapper>> proxy = new RpcProxy<GWTJahiaPageWrapper, List<GWTJahiaPageWrapper>>() {
            @Override
            protected void load(GWTJahiaPageWrapper parentPage, final AsyncCallback<List<GWTJahiaPageWrapper>> listAsyncCallback) {
                if (parentPage == null) {

                    service.getHomePageForCurrentUser(page.getPid(), page.getMode(), false, new AsyncCallback<GWTJahiaPageWrapper>() {
                        public void onFailure(Throwable throwable) {
                            listAsyncCallback.onFailure(throwable);
                        }

                        public void onSuccess(GWTJahiaPageWrapper gwtJahiaPageWrapper) {
                            List<GWTJahiaPageWrapper> l = new ArrayList<GWTJahiaPageWrapper>();
                            l.add(gwtJahiaPageWrapper);
                            listAsyncCallback.onSuccess(l);
                        }
                    });
                } else {
                    service.getSubPagesForCurrentUser(parentPage, listAsyncCallback);
                }
            }
        };

        // tree loader
        loader = new BaseTreeLoader<GWTJahiaPageWrapper>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaPageWrapper parent) {
                return parent.hasChildren() ;
            }
        };

        // tree store
        store = new TreeStore<GWTJahiaPageWrapper>(loader) ;

        binder = new TreeBinder<GWTJahiaPageWrapper>(this, store) ;
        binder.init() ;
        binder.setCaching(true);
        binder.setDisplayProperty("title");

        binder.setIconProvider(new ModelStringProvider<GWTJahiaPageWrapper>() {
            public String getStringValue(GWTJahiaPageWrapper node, String property) {
                if (!node.isSiteRoot()) {
                    return "icon-page" ;
                } else {
                    return null;
                }
            }
        });

        addListener(Events.SelectionChange, new Listener() {
            public void handleEvent(BaseEvent event) {
                TreeItem newSelection = getSelectedItem() ;
                if (lastSelection != newSelection) {
                    lastSelection = newSelection ;
                    Window.Location.replace(((GWTJahiaPageWrapper)newSelection.getModel()).getLink());
                }
            }
        });


        loader.load();
    }


}
