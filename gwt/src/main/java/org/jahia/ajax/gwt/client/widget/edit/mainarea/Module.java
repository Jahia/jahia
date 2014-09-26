/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
    protected String template;
    protected String scriptInfo;
    protected String sourceInfo;
    protected String headerText;
    protected Map<String,List<String>> moduleParams;
    protected Module parentModule;
    protected MainModule mainModule;
    protected String nodeTypes;
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


    public Module() {
        super();
    }

    protected Module(String id, String path, Element divElement, MainModule mainModule) {
        super();
        this.id = id;
        this.path = path;

        // we test for presence of attributes since the getAttribute method will never return null if the
        // attribute doesn't exist. See http://code.google.com/p/google-web-toolkit/issues/detail?id=1770
        if (divElement.hasAttribute("template")) {
            template = divElement.getAttribute("template");
        }
        if (divElement.hasAttribute("nodetypes")) {
            nodeTypes = divElement.getAttribute("nodetypes");
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

        this.mainModule = mainModule;
    }

    protected Module(String id, String path, Element divElement, MainModule mainModule, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;

        // we test for presence of attributes since the getAttribute method will never return null if the
        // attribute doesn't exist. See http://code.google.com/p/google-web-toolkit/issues/detail?id=1770
        if (divElement.hasAttribute("template")) {
            template = divElement.getAttribute("template");
        }
        if (divElement.hasAttribute("nodetypes")) {
            nodeTypes = divElement.getAttribute("nodetypes");
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

        this.mainModule = mainModule;
    }

    protected Module(String id, String path, String template, String nodeTypes, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;
        this.template = template;
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
        if (node.getNodeTypes().contains("jmix:markedForDeletionRoot")) {
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
        } else if (node.get("j:workInProgress") != null && (Boolean) node.get("j:workInProgress")) {
            overlayLabel = new HTML(Messages.get("label.workInProgress", "work in progress"));
            overlayLabel.setStyleName("workinprogress-overlay");
            opacity = "0.6";
            overlayColorText = "#39f";
        }
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public String getNodeTypes() {
        return nodeTypes;
    }

    public int getListLimit() {
        return listLimit;
    }

    public String getReferenceTypes() {
        return referenceTypes;
    }

    public String getTemplate() {
        return template;
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
}
