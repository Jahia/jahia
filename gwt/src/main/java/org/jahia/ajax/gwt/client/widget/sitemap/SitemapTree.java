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
package org.jahia.ajax.gwt.client.widget.sitemap;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.GWTJahiaPageWrapper;
import org.jahia.ajax.gwt.client.service.JahiaContentService;
import org.jahia.ajax.gwt.client.service.JahiaContentServiceAsync;
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
        final JahiaContentServiceAsync service = JahiaContentService.App.getInstance() ;

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
