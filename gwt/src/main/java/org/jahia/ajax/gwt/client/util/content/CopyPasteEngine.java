/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.util.content;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.PlaceholderModule;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ClipboardActionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author rfelden
 * @version 8 juil. 2008 - 17:13:07
 */
public class CopyPasteEngine {

    private static CopyPasteEngine m_instance = null ;
    private List<PlaceholderModule> placeholders = new ArrayList<PlaceholderModule>();
    
    // Copy-paste
    private final List<GWTJahiaNode> copiedNodes = new ArrayList<GWTJahiaNode>();
    private boolean cut ;

    public static CopyPasteEngine getInstance() {
        if (m_instance == null) {
            m_instance = new CopyPasteEngine() ;
        }
        return m_instance ;
    }

    protected CopyPasteEngine() {
        exportStaticMethod();
    }

    public void paste(final GWTJahiaNode m, final Linker linker, List<String> childNodeTypesToSkip) {
        if (!getCopiedNodes().isEmpty()) {
            JahiaContentManagementService
                    .App.getInstance().paste(JCRClientUtils.getPathesList(getCopiedNodes()), m.getPath(), null, isCut(), childNodeTypesToSkip, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    final String message = isCut() ? throwable
                            .getLocalizedMessage() : Messages.get("failure.paste.label") + "\n" + throwable
                            .getLocalizedMessage();
                    Window.alert(message);
                    if (linker instanceof ManagerLinker) {
                        ManagerLinker managerLinker = (ManagerLinker) linker;
                        managerLinker.getLeftComponent().unmask();
                        managerLinker.getTopRightComponent().unmask();
                    } else {
                        linker.loaded();
                    }
                }

                public void onSuccess(Object o) {
                    afterPaste(linker, m);
                }
            });
        }
    }
    public void pasteReference(final GWTJahiaNode m, final Linker linker) {
        JahiaContentManagementService
                .App.getInstance().pasteReferences(JCRClientUtils.getPathesList(getCopiedNodes()), m.getPath(), null, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                final String message = isCut() ? throwable
                        .getLocalizedMessage() : Messages.get("failure.paste.label") + "\n" + throwable
                        .getLocalizedMessage();
                Window.alert(message);
                if (linker instanceof ManagerLinker) {
                    ManagerLinker managerLinker = (ManagerLinker) linker;
                    managerLinker.getLeftComponent().unmask();
                    managerLinker.getTopRightComponent().unmask();
                } else {
                    linker.loaded();
                }
            }

            public void onSuccess(Object o) {
                afterPaste(linker, m);
            }
        });

    }

    private void afterPaste(Linker linker, GWTJahiaNode target) {
        linker.loaded();
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> newPaths = new ArrayList<String>();
        for (GWTJahiaNode node : getCopiedNodes()) {
            newPaths.add(target.getPath() + "/" +  node.getName());
        }
        data.put("node", getCopiedNodes().get(0));
        data.put("nodes", newPaths);
        if (linker instanceof ManagerLinker) {
            ManagerLinker managerLinker = (ManagerLinker) linker;
            managerLinker.getLeftComponent().unmask();
            managerLinker.getTopRightComponent().unmask();
            data.put(Linker.REFRESH_ALL,true);
            linker.refresh(data);
        } else if (isCut() && MainModule.getInstance().getPath().contains(getCopiedNodes().get(0).getPath())) {
            MainModule.staticGoTo(newPaths.get(0),MainModule.getInstance().getTemplate());
        } else {
            data.put(Linker.REFRESH_MAIN, true);
            linker.refresh(data);
        }
        onPastedPath();
    }

    public List<GWTJahiaNode> getCopiedNodes() {
        return copiedNodes;
    }

    public native void exportStaticMethod() /*-{
        var that = this;
        $wnd.getJahiaClipboard = function () {
            return  that.@org.jahia.ajax.gwt.client.util.content.CopyPasteEngine::getCopiedNodesAsJs()();
        }
        $wnd.getJahiaClipboardIsCut = function () {
            return  that.@org.jahia.ajax.gwt.client.util.content.CopyPasteEngine::isCut()();
        }
        $wnd.getJahiaClipboardClear = function () {
            that.@org.jahia.ajax.gwt.client.util.content.CopyPasteEngine::onPastedPath()();
        }
    }-*/;

    private JsArray<JavaScriptObject> getCopiedNodesAsJs() {
        List<JavaScriptObject> l = new ArrayList<JavaScriptObject>();
        for (GWTJahiaNode gwtJahiaNode : copiedNodes) {
            l.add(convertGwtNode(gwtJahiaNode.getName(), gwtJahiaNode.getPath(), gwtJahiaNode.getUUID(),
                    (gwtJahiaNode.getNodeTypes() != null && gwtJahiaNode.getNodeTypes().size() > 0) ? gwtJahiaNode.getNodeTypes().get(0) : null));
        }
        return JsArrayUtils.readOnlyJsArray(l.toArray(new JavaScriptObject[l.size()]));
    }

    private native JavaScriptObject convertGwtNode(String name, String path, String uuid, String nodeType) /*-{
        return { 'name': name, 'path': path, 'uuid': uuid, 'nodetype': nodeType};
    }-*/;

    private native void sendCopyEvent(JsArray<JavaScriptObject> copiedPaths, String type)  /*-{
        $wnd.dispatchEvent(new CustomEvent('jahia-copy', { 'detail': { 'nodes' : copiedPaths , 'type': type }}));
    }-*/;

    public void setCopiedNodes(List<GWTJahiaNode> copiedPaths) {
        cut = false ;
        this.copiedNodes.clear();
        this.copiedNodes.addAll(copiedPaths);
        ClipboardActionItem.setCopied(copiedPaths);
        updatePlaceholders();
        sendCopyEvent(getCopiedNodesAsJs(), "copy");
    }

    public void setCutPaths(List<GWTJahiaNode> cutPaths) {
        this.copiedNodes.clear();
        this.copiedNodes.addAll(cutPaths);
        cut = true ;
        ClipboardActionItem.setCopied(cutPaths);
        updatePlaceholders();
        sendCopyEvent(getCopiedNodesAsJs(), "cut");
    }

    public void onPastedPath() {
        ClipboardActionItem.removeCopied(copiedNodes);
        copiedNodes.clear();
        cut = false ;
        updatePlaceholders();
        sendCopyEvent(getCopiedNodesAsJs(), "copy");
    }

    public boolean isCut() {
        return cut ;
    }

    public boolean canCopyTo(GWTJahiaNode dest) {
        if (dest == null) {
            return false ;
        }
        if (copiedNodes == null) {
            return false;
        }

        for (GWTJahiaNode copiedPath : copiedNodes) {
            if ((dest.getPath()+"/").startsWith(copiedPath.getPath()+"/")) {
                return false;
            }
            if (isCut() && copiedPath.getPath().substring(0,copiedPath.getPath().lastIndexOf('/')).equals(dest.getPath())) {
                return false;
            }

            // check only first node ..
            return true;    
        }
        return true;
    }

    public boolean checkNodeType(String nodetypes) {
        List<GWTJahiaNode> sources = getCopiedNodes();
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (!sources.isEmpty()) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.isNodeType(type)
                                || (source.isReference() && source.getReferencedNode() != null && source.getReferencedNode().isNodeType(type))) {
                            nodeAllowed = true;
                            break;
                        } else if(type.equals("jnt:contentReference") && source.isNodeType("jmix:droppableContent")) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

    public boolean canPasteAsReference() {
        if (isCut()) {
            return false;
        }
        List<GWTJahiaNode> sources = getCopiedNodes();
        if (!sources.isEmpty()) {
            for (GWTJahiaNode source : sources) {
                if (source.isReference()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updatePlaceholders() {
        for (PlaceholderModule module : placeholders) {
            module.updatePasteButton();
        }
    }
    
    public void addPlaceholder(PlaceholderModule module) {
        placeholders.add(module);
    }
}
