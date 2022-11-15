/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Module where a content can be created by clicking on a button or drag'n'dropping on it a content type
 * <p>
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 */
public class PlaceholderModule extends Module {
    private LayoutContainer panel;
    private LayoutContainer pasteButton;
    private LayoutContainer pasteAsReferenceButton;

    private static int MIN_WIDTH = 100;

    public PlaceholderModule(String id, String path, Element divElement, final MainModule mainModule) {
        super(id, path, divElement, mainModule, new FlowLayout());

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }

        html = new HTML("");
    }

    @Override
    public void onParsed() {
        panel = new LayoutContainer(new RowLayout(Style.Orientation.VERTICAL));
        add(panel);
    }

    @Override
    public void onNodeTypesLoaded() {
        if (mainModule.getConfig().isDragAndDropEnabled()) {
            DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.PLACEHOLDER_TYPE);
            target.setOperation(DND.Operation.COPY);
            target.setFeedback(DND.Feedback.INSERT);

            target.addDNDListener(mainModule.getEditLinker().getDndListener());
        }

        if (getParentModule().getChildCount() >= getParentModule().getListLimit() && getParentModule().getListLimit() != -1) {
            return;
        }

        if (getParentModule() instanceof AreaModule && getParentModule().getChildCount() == 0
                && ((AreaModule) getParentModule()).editable) {
            ((AreaModule) getParentModule()).setEnabledEmptyArea();
        }

        String[] nodeTypesArray = null;
        if (getParentModule() != null && getParentModule().getNodeTypes() != null) {
            nodeTypesArray = getParentModule().getNodeTypes().split(" ");
        }
        if ((getNodeTypes() != null) && (getNodeTypes().length() > 0)) {
            nodeTypesArray = getNodeTypes().split(" ");
        }

        if (nodeTypesArray != null) {
            // by default display all types resolved.
            final Set<String> displayedNodeTypes = new HashSet<>(Arrays.asList(nodeTypesArray));

            // in case of nodetypes are from the parent but current module has its own restrictions
            if (nodeTypes != null && nodeTypes.length() > 0) {
                final List<String> filter = new ArrayList<>(Arrays.asList(nodeTypes.split(" ")));
                displayedNodeTypes.clear();
                displayedNodeTypes.addAll(Arrays.stream(nodeTypesArray).filter(filter::contains).collect(Collectors.toSet()));
            }

            // in case there are more than MAX_NODETYPES_DISPLAYED types to display, display "Any content" and let the content type selector
            // do the work.
            final int maxNtDisplayed = mainModule.getConfig().getCreateChildrenDirectButtonsLimit() < 1 ? Module.MAX_NODETYPES_DISPLAYED : mainModule.getConfig().getCreateChildrenDirectButtonsLimit();
            final boolean displayAnyContent = displayedNodeTypes.size() > maxNtDisplayed;
            if (displayAnyContent) {
                displayedNodeTypes.clear();
                displayedNodeTypes.add("jmix:droppableContent");
            }

            for (final String currentNodeType : displayedNodeTypes) {
                GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(currentNodeType);
                if (nodeType != null) {
                    Boolean canUseComponentForCreate = nodeType.get("canUseComponentForCreate");
                    if (canUseComponentForCreate != null && !canUseComponentForCreate) {
                        continue;
                    }
                }
                Image icon = ContentModelIconProvider.getInstance().getIcon(nodeType).createImage();
                icon.setTitle(nodeType != null ? nodeType.getLabel() : currentNodeType);
                LayoutContainer p = new HorizontalPanel();
                p.add(icon);

                Text label = new Text(nodeType != null ? nodeType.getLabel() : currentNodeType);
                if (getWidth() >= MIN_WIDTH || getWidth() == 0) {
                    p.add(label);
                } else {
                    p.setTitle(label.getText());
                }

                final String effectiveNodeTypes = displayAnyContent ? String.join(" ", nodeTypesArray) : currentNodeType;

                p.sinkEvents(Event.ONCLICK);
                p.addStyleName("button-placeholder");
                p.addListener(Events.OnClick, new Listener<ComponentEvent>() {

                    @Override
                    public void handleEvent(ComponentEvent be) {
                        final GWTJahiaNode parentNode = getParentModule().getNode();
                        if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && (!parentNode.isLocked()
                                || parentNode.isLockAllowsAdd())) {
                            String nodeName = null;
                            if ((path != null) && !"*".equals(path) && !path.startsWith("/")) {
                                nodeName = path;
                            }
                            ContentActions.showContentWizard(mainModule.getEditLinker(), effectiveNodeTypes, parentNode, nodeName, true,
                                    displayAnyContent ? null : displayedNodeTypes, false, nodeName != null);
                        }
                    }
                });
                panel.add(p, new RowData());
            }

            Image icon = ToolbarIconProvider.getInstance().getIcon("paste").createImage();
            icon.setTitle(Messages.get("label.paste", "Paste"));
            pasteButton = new HorizontalPanel();
            pasteButton.add(icon);

            Text pasteLabel = new Text(Messages.get("label.paste", "Paste"));
            if (getWidth() >= MIN_WIDTH) {
                pasteButton.add(pasteLabel);
            } else {
                pasteButton.setTitle(pasteLabel.getTitle());
            }
            pasteButton.sinkEvents(Event.ONCLICK);
            pasteButton.addStyleName("button-placeholder");

            pasteButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    GWTJahiaNode parentNode = getParentModule().getNode();
                    if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && (!parentNode.isLocked()
                            || parentNode.isLockAllowsAdd())) {
                        String nodeName = null;
                        if ((path != null) && !"*".equals(path) && !path.startsWith("/")) {
                            nodeName = path;
                        }
                        CopyPasteEngine.getInstance().paste(parentNode, mainModule.getEditLinker(), null, nodeName);
                    }
                }
            });
            AbstractImagePrototype pasteAsReferenceIcon = ToolbarIconProvider.getInstance().getIcon("pasteReference");
            pasteAsReferenceButton = new HorizontalPanel();
            pasteAsReferenceButton.add(pasteAsReferenceIcon.createImage());

            Text pasteReferenceLabel = new Text(Messages.get("label.pasteReference", "Paste Reference"));
            if (getWidth() >= MIN_WIDTH) {
                pasteAsReferenceButton.add(pasteReferenceLabel);
            } else {
                pasteAsReferenceButton.setTitle(pasteReferenceLabel.getTitle());
            }
            pasteAsReferenceButton.sinkEvents(Event.ONCLICK);
            pasteAsReferenceButton.addStyleName("button-placeholder");

            pasteAsReferenceButton.addListener(Events.OnClick, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent be) {
                    GWTJahiaNode parentNode = getParentModule().getNode();
                    if (parentNode != null && PermissionsUtils.isPermitted("jcr:addChildNodes", parentNode) && (!parentNode.isLocked()
                            || parentNode.isLockAllowsAdd())) {
                        CopyPasteEngine.getInstance().pasteReference(parentNode, mainModule.getEditLinker());
                    }
                }
            });

            CopyPasteEngine.getInstance().addPlaceholder(this);

            updatePasteButton();
            panel.add(pasteButton, new RowData());
            panel.add(pasteAsReferenceButton, new RowData());
            panel.layout();
        }
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public void updatePasteButton() {
        String restrictToNodeTypes = getNodeTypes() != null && !getNodeTypes().isEmpty() ? getNodeTypes() : parentModule.getNodeTypes();
        if (!CopyPasteEngine.getInstance().getCopiedNodes().isEmpty()) {
             boolean hasPastePermission =PermissionsUtils.isPermitted("pasteAction", getParentModule().getNode());
            pasteButton.setVisible(CopyPasteEngine.getInstance().checkNodeType(restrictToNodeTypes, false) && CopyPasteEngine.getInstance()
                    .canCopyTo(getParentModule().getNode()) && hasPastePermission);
            pasteAsReferenceButton.setVisible(
                    CopyPasteEngine.getInstance().checkNodeType(restrictToNodeTypes, true) && CopyPasteEngine.getInstance()
                            .canPasteAsReference() && parentModule.isAllowReferences() && isAllowReferences() && hasPastePermission);
        } else {
            pasteButton.setVisible(false);
            pasteAsReferenceButton.setVisible(false);
        }
    }
}
