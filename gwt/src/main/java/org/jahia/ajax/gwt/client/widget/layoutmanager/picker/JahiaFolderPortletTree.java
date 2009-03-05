package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
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

        final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();

        // data proxy
        RpcProxy<GWTJahiaNode, List<GWTJahiaNode>> proxy = new RpcProxy<GWTJahiaNode, List<GWTJahiaNode>>() {
            @Override
            protected void load(GWTJahiaNode gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> callback) {
                if (gwtJahiaFolder == null) {
                    service.getRoot(JCRClientUtils.ALL_MASHUPS, JCRClientUtils.FOLDER_NODETYPES, null, null, null, callback);
                } else {
                    service.ls(gwtJahiaFolder, JCRClientUtils.FOLDER_NODETYPES, null, null, null, false, callback);
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
        tree.addListener(Events.SelectionChange, new Listener() {
            public void handleEvent(BaseEvent event) {
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
