/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Map;

/**
 * Interface defining what is a mdoule on a rendered page in edit mode
 * A module could be selected, edited, dragged etc.
 */
public abstract class Module extends LayoutContainer {

    protected String id;
    protected GWTJahiaNode node;
    protected HTML html;
    protected String path;
    protected String template;
    protected String scriptInfo;
    protected Map<String,String> moduleParams;
    protected Module parentModule;
    protected MainModule mainModule;
    protected String nodeTypes;
    protected int listLimit;
    protected String referenceTypes;
    protected boolean isDraggable = false;
    protected int depth;
    protected boolean selectable;
    protected Header head;
    protected int childCount = 0;

    public Module() {
    }

    protected Module(String id, String path, Element divElement, MainModule mainModule) {
        super();
        this.id = id;
        this.path = path;

        template = DOM.getElementAttribute(divElement, "template");
        nodeTypes = DOM.getElementAttribute(divElement, "nodetypes");
        listLimit = !"".equals(DOM.getElementAttribute(divElement, "listlimit")) ? Integer.parseInt(DOM.getElementAttribute(divElement, "listlimit")): -1;
        referenceTypes = DOM.getElementAttribute(divElement, "referenceTypes");
        scriptInfo = DOM.getElementAttribute(divElement, "scriptInfo");

        this.mainModule = mainModule;
    }

    protected Module(String id, String path, Element divElement, MainModule mainModule, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;

        template = DOM.getElementAttribute(divElement, "template");
        nodeTypes = DOM.getElementAttribute(divElement, "nodetypes");
        listLimit = !"".equals(DOM.getElementAttribute(divElement, "listlimit")) ? Integer.parseInt(DOM.getElementAttribute(divElement, "listlimit")): -1;
        referenceTypes = DOM.getElementAttribute(divElement, "referenceTypes");
        scriptInfo = DOM.getElementAttribute(divElement, "scriptInfo");

        this.mainModule = mainModule;
    }

    protected Module(String id, String path, String template, String nodeTypes, Layout layout) {
        super(layout);
        this.id = id;
        this.path = path;
        this.template = template;
        this.nodeTypes = nodeTypes;
    }

    public void onParsed() {

    }

    public void onNodeTypesLoaded() {

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

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
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
        head.setText(headerText);
    }

    public String getScriptInfo() {
        return scriptInfo;
    }
}
