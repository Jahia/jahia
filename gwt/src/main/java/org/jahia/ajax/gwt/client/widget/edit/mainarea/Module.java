/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;
import java.util.Map;

/**
 * Interface defining what is a module on a rendered page in edit mode
 * A module could be selected, edited, dragged etc.
 */
public abstract class Module extends LayoutContainer {

    protected String id;
    protected GWTJahiaNode node;
    protected HTML html;
    protected String path;
    protected String scriptInfo;
    protected String sourceInfo;
    protected String headerText;
    protected Map<String, List<String>> moduleParams;
    protected Module parentModule;
    protected MainModule mainModule;
    protected String nodeTypes;
    protected boolean allowReferences = true;
    protected int listLimit = -1;
    protected String referenceTypes;
    protected boolean isDraggable = false;
    protected int depth;
    protected boolean selectable;
    protected Header head;
    protected int childCount = 0;
    protected int visibleChildCount = 0;
    protected HTML overlayLabel = null;
    protected String opacity = "";
    protected String overlayColorText = "";
    protected String translatableFromLanguage = null;

    public static final int MAX_NODETYPES_DISPLAYED = 5;

    public Module() {
        super();
    }

    protected Module(String id, String path, Element divElement, MainModule mainModule) {
        super();
        this.id = id;
        this.path = path;

        // we test for presence of attributes since the getAttribute method will never return null if the
        // attribute doesn't exist. See http://code.google.com/p/google-web-toolkit/issues/detail?id=1770
        if (divElement.hasAttribute("nodetypes")) {
            nodeTypes = divElement.getAttribute("nodetypes");
        }
        if (divElement.hasAttribute("allowReferences")) {
            allowReferences = Boolean.valueOf(divElement.getAttribute("allowReferences"));
        }
        listLimit = !"".equals(DOM.getElementAttribute(divElement, "listlimit")) ? Integer.parseInt(DOM.getElementAttribute(divElement, "listlimit")): -1;
        if (divElement.hasAttribute("referenceTypes")) {
            referenceTypes = divElement.getAttribute("referenceTypes");
            if ("none".equals(referenceTypes)) {
                referenceTypes = "";
            }
        } else {
            referenceTypes = null;
        }
        if (divElement.hasAttribute("scriptInfo")) {
            scriptInfo = divElement.getAttribute("scriptInfo");
        }
        if (divElement.hasAttribute("sourceInfo")) {
            sourceInfo = divElement.getAttribute("sourceInfo");
        }
        if (divElement.hasAttribute("translatable")) {
            translatableFromLanguage = divElement.getAttribute("translatable");
        }

        this.mainModule = mainModule;
    }

    protected Module(String id, String path, Element divElement, MainModule mainModule, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;

        // we test for presence of attributes since the getAttribute method will never return null if the
        // attribute doesn't exist. See http://code.google.com/p/google-web-toolkit/issues/detail?id=1770
        if (divElement.hasAttribute("nodetypes")) {
            nodeTypes = divElement.getAttribute("nodetypes");
        }
        if (divElement.hasAttribute("allowReferences")) {
            allowReferences = Boolean.valueOf(divElement.getAttribute("allowReferences"));
        }
        listLimit = !"".equals(DOM.getElementAttribute(divElement, "listlimit")) ? Integer.parseInt(DOM.getElementAttribute(divElement, "listlimit")): -1;
        if (divElement.hasAttribute("referenceTypes")) {
            referenceTypes = divElement.getAttribute("referenceTypes");
            if ("none".equals(referenceTypes)) {
                referenceTypes = "";
            }
        } else {
            referenceTypes = null;
        }
        if (divElement.hasAttribute("scriptInfo")) {
            scriptInfo = divElement.getAttribute("scriptInfo");
        }
        if (divElement.hasAttribute("translatable")) {
            translatableFromLanguage = divElement.getAttribute("translatable");
        }

        this.mainModule = mainModule;
    }

    protected Module(String id, String path, String nodeTypes, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;
        this.nodeTypes = nodeTypes;
    }

    /**
     * Callback that is executed when the DOM module for this module is parsed.
     */
    public void onParsed() {
        // will be overridden in sub-classes
    }

    /**
     * Callback that is executed when the required node types are loaded from the server for this module.
     */
    public void onNodeTypesLoaded() {
        // will be overridden in sub-classes
    }

    public Module getParentModule() {
        return parentModule;
    }

    public void addChild(Module childModule) {
        this.childCount++;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
        if (parentModule != null) {
            parentModule.addChild(this);
        }
    }

    public int getChildCount() {
        return childCount;
    }

    public String getModuleId() {
        return id;
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public String getOverlayColorText() {
        return overlayColorText;
    }

    public HTML getOverlayLabel() {
        return overlayLabel;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
        overlayLabel = null;
        overlayColorText = "";
        opacity = "";
        if (node.isMarkedForDeletionRoot()) {
            overlayLabel = new HTML(Messages.get("label.deleted", "Deleted"));
            overlayLabel.setStyleName("deleted-overlay");
            overlayColorText = "#f00";
            opacity = "0.4";
        } else if (node.getInvalidLanguages() != null && node.getInvalidLanguages().contains(getMainModule().getEditLinker().getLocale())) {
            overlayLabel = new HTML(Messages.get("label.validLanguages.overlay",
                    "Not visible content"));
            overlayLabel.setStyleName("deleted-overlay");
            opacity = "0.4";
            overlayColorText = "#f00";
        } else if (node.isInWorkInProgress(node.getLanguageCode())) {
            applyWipOverlay();
        }
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public String getNodeTypes() {
        return nodeTypes;
    }

    public boolean isAllowReferences() {
        return allowReferences;
    }

    public int getListLimit() {
        return listLimit;
    }

    public String getReferenceTypes() {
        return referenceTypes;
    }

    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public Header getHeader() {
        return head;
    }

    protected void setHeaderText(String headerText) {
        this.headerText = headerText;
        head.setText(headerText);
    }

    public String getScriptInfo() {
        return scriptInfo;
    }

    public String getSourceInfo() {
        return sourceInfo;
    }

    public Element getInnerElement() {
        return html.getElement();
    }

    private void applyWipOverlay() {
        overlayLabel = new HTML(Messages.get("label.workInProgress", "work in progress"));
        overlayLabel.setStyleName("workinprogress-overlay");
        opacity = "0.6";
        overlayColorText = "#39f";
    }

    public String getTranslatableFromLanguage() {
        return translatableFromLanguage;
    }
}
