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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.core.client.JavaScriptObject;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.HashMap;
import java.util.Map;

/**
 * An action item for executing JS
 * 
 */
public class JSActionItem extends NodeTypeAwareBaseActionItem {
    private static final long serialVersionUID = -1317342305404063292L;

    private String init;
    private String execute;
    private String handleNewSelection;
    private String handleNewMainNodeLoaded;
    private String selectionTarget;


    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (init != null) {
            doCall(init, getNative(this));
        }
    }

    public void onComponentSelection() {
        if (execute != null) {
            doCall(execute, getSelection(selectionTarget, linker));
        }
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        if (handleNewMainNodeLoaded != null) {
            doCall(handleNewMainNodeLoaded, node.getPath());
        }
    }

    @Override
    public void handleNewLinkerSelection() {
        if (handleNewSelection != null) {
            doCall(handleNewSelection, getSelection(selectionTarget, linker));
        } 
        setEnabled(isNodeTypeAllowed());
    }

    public boolean isNodeTypeAllowed() {
        if (selectionTarget == null || selectionTarget.equals("single")) {
            return isNodeTypeAllowed(linker.getSelectionContext().getSingleSelection());
        } else if (selectionTarget.equals("multiple")) {
            return isNodeTypeAllowed(linker.getSelectionContext().getMultipleSelection());
        } else if (selectionTarget.equals("main")) {
            return isNodeTypeAllowed(linker.getSelectionContext().getMainNode());
        }
        return false;
    }

    public void refresh() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(Linker.REFRESH_MAIN, true);
        linker.refresh(data);
    }


    private static String getSelection(String selectionTarget, Linker linker) {
        if (selectionTarget == null || selectionTarget.equals("single")) {
            GWTJahiaNode singleSelection = linker.getSelectionContext().getSingleSelection();
            return singleSelection != null ? singleSelection.getPath() : null;
        } else if (selectionTarget.equals("multiple")) {
//            List<String> paths = new ArrayList<>();
//            List<GWTJahiaNode> nodes = linker.getSelectionContext().getMultipleSelection();
//            if (nodes != null) {
//                for (GWTJahiaNode node : nodes) {
//                    paths.add(node.getPath());
//                }
//            }
//            return  paths;
        } else if (selectionTarget.equals("main")) {
            GWTJahiaNode node = linker.getSelectionContext().getMainNode();
            return node != null ? node.getPath() : null;
        }
        return null;
    }

    public static native void doCall(String key, Object param) /*-{
        eval('$wnd.' + key)(param);
    }-*/;

    public void setInit(String init) {
        this.init = init;
    }

    public void setExecute(String execute) {
        this.execute = execute;
    }

    public void setHandleNewSelection(String handleNewSelection) {
        this.handleNewSelection = handleNewSelection;
    }

    public void setHandleNewMainNodeLoaded(String handleNewMainNodeLoaded) {
        this.handleNewMainNodeLoaded = handleNewMainNodeLoaded;
    }

    public void setSelectionTarget(String selectionTarget) {
        this.selectionTarget = selectionTarget;
    }

    public static native JavaScriptObject getNative(JSActionItem actionItem) /*-{
        return {
            'setTitle': function (text) {
                actionItem.@org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem::updateTitle(*)(text);
            },
            'setEnabled': function(b) {
                actionItem.@org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem::setEnabled(*)(b);
            },
            'setVisible': function(b) {
                actionItem.@org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem::setVisible(*)(b);
            },
            'isNodeTypeAllowed': function() {
                actionItem.@org.jahia.ajax.gwt.client.widget.toolbar.action.JSActionItem::isNodeTypeAllowed()();
            },
            'refresh': function() {
                actionItem.@org.jahia.ajax.gwt.client.widget.toolbar.action.JSActionItem::refresh()();
            }
        }
    }-*/;

}

