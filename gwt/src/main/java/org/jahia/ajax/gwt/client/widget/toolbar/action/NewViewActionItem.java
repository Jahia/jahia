/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreeGridEvent;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
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

    private static int TEMPLATE_TYPE_FOLDER_TOKEN = 2;
    private static int VIEW_FILE_FOLDER_TOKEN = 3;

    private static final String TEMPLATE = "jnt:template";
    private static final String TEMPLATE_FILE = "jnt:templateFile";
    private static final String VIEW_FILE = "jnt:viewFile";

    private transient Map<String, GWTJahiaNodeType> fileNodeTypes;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        parentTypesAsList = Arrays.asList("jnt:moduleVersionFolder", "jnt:nodeTypeFolder", "jnt:templateTypeFolder");
    }

    @Override
    public void onComponentSelection() {
        if (fileNodeTypes == null) {
            linker.loading(Messages.get("label.loading", "Loading"));
            JahiaContentManagementService.App.getInstance().getNodeTypes(Arrays.asList(TEMPLATE_FILE, VIEW_FILE), new BaseAsyncCallback<List<GWTJahiaNodeType>>() {

                @Override
                public void onSuccess(List<GWTJahiaNodeType> gwtJahiaNodeTypes) {
                    fileNodeTypes = new HashMap<String, GWTJahiaNodeType>();
                    for (GWTJahiaNodeType gwtJahiaNodeType : gwtJahiaNodeTypes) {
                        fileNodeTypes.put(gwtJahiaNodeType.getName(), gwtJahiaNodeType);
                    }
                    newView(linker, true);
                }
            });
        } else {
            newView(linker, false);
        }
    }

    @SuppressWarnings("unchecked")
    private void newView(final Linker linker, boolean isLoading) {
        final GWTJahiaNode selectedNode = linker.getSelectionContext().getSingleSelection();

        if (selectedNode.getNodeTypes().contains("jnt:moduleVersionFolder")) {
            // Open popup to select nodeType

            ArrayList<String> paths = new ArrayList<String>();
            paths.add(JahiaGWTParameters.getSiteNode().getPath());
            for (String s : (List<String>) JahiaGWTParameters.getSiteNode().getProperties().get("j:resolvedDependencies")) {
                for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                    if (n.getName().equals(s)) {
                        paths.add(n.getPath());

                    }
                }
            }

            if (!isLoading) {
                linker.loading(Messages.get("label.loading", "Loading"));
            }
            JahiaContentManagementService.App.getInstance().getContentTypesAsTree(Arrays.asList("nt:base"), null, true,
                    new BaseAsyncCallback<List<GWTJahiaNodeType>>() {

                        @Override
                        public void onSuccess(List<GWTJahiaNodeType> result) {
                            linker.loaded();
                            final com.extjs.gxt.ui.client.widget.Window popup = new com.extjs.gxt.ui.client.widget.Window();
                            popup.addStyleName("new-view-modal");
                            popup.setHeadingHtml(Messages.get("label.addView", "Add view"));
                            popup.setHeight(400);
                            popup.setWidth(350);
                            popup.setModal(true);
                            popup.setLayout(new FillLayout());
                            ContentTypeTree contentTypeTree = new ContentTypeTree();
                            contentTypeTree.fillStore(result);
                            TreeGrid<?> treeGrid = contentTypeTree.getTreeGrid();
                            treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
                            treeGrid.addListener(Events.OnDoubleClick, new Listener<TreeGridEvent<GWTJahiaNodeType>>() {
                                public void handleEvent(TreeGridEvent<GWTJahiaNodeType> baseEvent) {
                                    GWTJahiaNodeType gwtJahiaNodeType = baseEvent.getModel();
                                    if (gwtJahiaNodeType != null && linker != null) {
                                        GWTJahiaNodeType engineNodeType = TEMPLATE.equals(gwtJahiaNodeType.getName()) ?
                                                fileNodeTypes.get(TEMPLATE_FILE) : fileNodeTypes.get(VIEW_FILE);
                                        createEngine(engineNodeType, selectedNode, gwtJahiaNodeType.getName());
                                        popup.hide();
                                    }
                                }
                            });
                            popup.add(contentTypeTree);
                            popup.show();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            linker.loaded();
                            super.onFailure(caught);
                        }
                    }

            );


        } else {
            if (isLoading) {
                linker.loaded();
            }
            GWTJahiaNodeType fileNodeType = TEMPLATE.equals(findNodeType(selectedNode)) ?
                    fileNodeTypes.get(TEMPLATE_FILE) : fileNodeTypes.get(VIEW_FILE);
            createEngine(fileNodeType, selectedNode, findNodeType(selectedNode));
        }
    }

    private void createEngine(GWTJahiaNodeType nodeType, GWTJahiaNode selectedNode, String targetName) {
        HashMap<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
        props.put("nodeTypeName", new GWTJahiaNodeProperty("nodeTypeName", targetName));
        EngineLoader.showCreateEngine(linker, selectedNode, nodeType, props, targetName.replaceAll(":", "_"), false, null);
    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n = lh.getSingleSelection();

        boolean enabled = !"".equals(n.getChildConstraints().trim())
                && !lh.isLocked()
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions());

        if (enabled) {
            String  title = getGwtToolbarItem().getTitle();
            if (n.isNodeType("jnt:moduleVersionFolder")) {
                updateTitle(title);
            } else {
                String nodetype = findNodeType(n);
                if (nodetype.equals("")) {
                    // the node type has not been resolved
                    enabled = false;
                } else {
                    updateTitle(title + (!nodetype.equals("")?(" : " + nodetype):""));
                }

            }
        }
        setEnabled(enabled) ;
    }

    private String findNodeType(GWTJahiaNode n) {

        String[] splittedPath = n.getPath().split("/");
        if (n.isNodeType("jnt:nodeTypeFolder")) {
            return n.getName().replace("_", ":");
        } else if (splittedPath.length > TEMPLATE_TYPE_FOLDER_TOKEN && n.isNodeType("jnt:templateTypeFolder")) {
            return  splittedPath[splittedPath.length - TEMPLATE_TYPE_FOLDER_TOKEN].replace("_", ":");
        }else if (splittedPath.length > VIEW_FILE_FOLDER_TOKEN  && (n.isNodeType(VIEW_FILE) || n.isNodeType(TEMPLATE_FILE))) {
            return splittedPath[splittedPath.length - VIEW_FILE_FOLDER_TOKEN].replace("_", ":");
        } else if (n.isNodeType("jnt:nodeType")) {
            return n.getName();
        }
        return "";
    }

}
