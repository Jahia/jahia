/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeTree;

import java.util.*;

public class NewViewActionItem extends BaseActionItem  {

    protected List<String> parentTypesAsList;

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        parentTypesAsList = Arrays.asList("jnt:moduleVersionFolder", "jnt:nodeTypeFolder", "jnt:templateTypeFolder");
    }

    public void onComponentSelection() {
        final GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();
        String parentType = selectedNode.getNodeTypes().get(0);
        String[] filePath = selectedNode.getPath().split("/");
        final GWTJahiaNodeType fileNodeType = new GWTJahiaNodeType("jnt:viewFile");
        if (!"modulesFileSystem".equals(filePath[1]) || (filePath.length > 4 && !filePath[4].contains("_"))) {
            // Open popup to select nodeType

            ArrayList<String> paths = new ArrayList<String>();
            paths.add(JahiaGWTParameters.getSiteNode().getPath());
            for (String s : (List<String>) JahiaGWTParameters.getSiteNode().getProperties().get("j:dependencies")) {
                for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                    if (n.getName().equals(s)) {
                        paths.add(n.getPath());

                    }
                }
            }


            JahiaContentManagementService.App.getInstance().getContentTypesAsTree(paths, Arrays.asList("nt:base"), Arrays.asList("name"), true,  false,
                    new BaseAsyncCallback<List<GWTJahiaNode>>() {
                        public void onSuccess(List<GWTJahiaNode> result) {
                            final com.extjs.gxt.ui.client.widget.Window popup = new com.extjs.gxt.ui.client.widget.Window();
                            popup.setHeading(Messages.get("label.addView", "Add view"));
                            popup.setHeight(200);
                            popup.setWidth(350);
                            popup.setModal(true);
                            popup.setLayout(new FillLayout());
                            ContentTypeTree contentTypeTree = new ContentTypeTree();
                            contentTypeTree.fillStore(result);
                            TreeGrid treeGrid = contentTypeTree.getTreeGrid();
                            treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
                            treeGrid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
                                public void handleEvent(BaseEvent baseEvent) {
                                    GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) (((TreeGridEvent) baseEvent).getModel()).get("componentNodeType");
                                    if (gwtJahiaNodeType != null && linker != null && !gwtJahiaNodeType.isMixin()) {
                                        createEngine(fileNodeType, selectedNode, gwtJahiaNodeType.getName().replaceAll(":", "_"));
                                        popup.hide();
                                    }
                                }
                            });
                            popup.add(contentTypeTree);
                            popup.show();
                        }
                    }
            );


        } else {
            createEngine(fileNodeType,selectedNode,filePath[4]);
        }
    }

    private void createEngine(GWTJahiaNodeType nodeType, GWTJahiaNode selectedNode, String targetName) {

        EngineLoader.showCreateEngine(linker, selectedNode, nodeType, new HashMap<String, GWTJahiaNodeProperty>(), targetName, false);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n = lh.getSingleSelection();
        boolean enabled = !"".equals(n.getChildConstraints().trim())
                && !lh.isLocked()
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions());
        setEnabled(enabled) ;
        if (enabled && n.getPath().startsWith("/modulesFileSystem") && n.getName().contains("_")) {
            updateTitle(getGwtToolbarItem().getTitle() + " : " + n.getDisplayName());
        } else {
            updateTitle(getGwtToolbarItem().getTitle());
        }
    }

}
