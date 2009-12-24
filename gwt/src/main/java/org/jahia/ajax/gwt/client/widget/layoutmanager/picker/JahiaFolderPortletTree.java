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
package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.TreeEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 15 janv. 2009
 * Time: 14:25:57
 */
public class JahiaFolderPortletTree extends LayoutContainer {

    public JahiaFolderPortletTree() {
    }


    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        setBorders(false);
        setStyleAttribute("background", "none");
        setLayout(new FlowLayout(10));

        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<List<GWTJahiaNode>> proxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> callback) {
                if (gwtJahiaFolder == null) {
                    service.getRoot(JCRClientUtils.GLOBAL_REPOSITORY, JCRClientUtils.FOLDER_NODETYPES, null, null, null, callback);
                } else {
                    service.ls(JCRClientUtils.GLOBAL_REPOSITORY,(GWTJahiaNode) gwtJahiaFolder, JCRClientUtils.FOLDER_NODETYPES, null, null, false, callback);
                }

            }
        };

        // tree loader
        TreeLoader loader = new BaseTreeLoader(proxy) {
            @Override
            public boolean hasChildren(ModelData parent) {
                return parent instanceof GWTJahiaNode;
            }
        };

        // trees store
        TreeStore<GWTJahiaNode> store = new TreeStore<GWTJahiaNode>(loader);
        store.setStoreSorter(new StoreSorter<GWTJahiaNode>() {

            @Override
            public int compare(Store store, GWTJahiaNode m1, GWTJahiaNode m2, String property) {
                boolean m1Folder = m1 instanceof GWTJahiaNode;
                boolean m2Folder = m2 instanceof GWTJahiaNode;

                if (m1Folder && !m2Folder) {
                    return -1;
                } else if (!m1Folder && m2Folder) {
                    return 1;
                }

                return super.compare(store, m1, m2, property);
            }
        });

        final Tree tree = new Tree();
        tree.setStyleAttribute("background", "none");
        final TreeBinder<GWTJahiaNode> binder = new TreeBinder<GWTJahiaNode>(tree, store);
        binder.setIconProvider(new ModelStringProvider<GWTJahiaNode>() {

            public String getStringValue(GWTJahiaNode model, String property) {
                if (!(model instanceof GWTJahiaNode)) {
                    String ext = model.getName().substring(model.getName().lastIndexOf(".") + 1);

                    // new feature, using image paths rather than style names
                    if ("xml".equals(ext)) {
                        return "images/icons/page_white_code.png";
                    } else if ("java".equals(ext)) {
                        return "images/icons/page_white_cup.png";
                    } else if ("html".equals(ext)) {
                        return "images/icons/html.png";
                    } else {
                        return "images/icons/page_white.png";
                    }
                }
                return null;
            }

        });
        tree.addListener(Events.SelectionChange, new Listener<TreeEvent>() {
            public void handleEvent(TreeEvent event) {
                List<GWTJahiaNode> gwtJahiaNodes = binder.getSelection();
                if (gwtJahiaNodes != null && gwtJahiaNodes.size() > 0) {
                    JahiaPortalManager.getInstance().getPortletPicker().loadContent(gwtJahiaNodes.get(0));
                }
            }
        });
        binder.setDisplayProperty("displayName");

        loader.load(null);
        add(tree);
    }
}
