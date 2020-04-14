/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
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

import java.util.*;

/**
 * @author rfelden
 * @version 8 juil. 2008 - 17:13:07
 */
public class CopyPasteEngine {

    private static CopyPasteEngine instance = null;
    private List<PlaceholderModule> placeholders = new ArrayList<PlaceholderModule>();

    // Copy-paste
    private final List<GWTJahiaNode> copiedNodes = new ArrayList<GWTJahiaNode>();
    private final Storage storage;
    private String previousStoredValue;

    private boolean cut;

    public static CopyPasteEngine getInstance() {
        if (instance == null) {
            instance = new CopyPasteEngine();
        }
        return instance;
    }

    protected CopyPasteEngine() {
        exportStaticMethod();

        storage = Storage.getLocalStorageIfSupported();

        if (storage != null) {
            Timer t = new Timer() {
                @Override
                public void run() {
                    String clipboardString = storage.getItem("jahia-clipboard");
                    if (clipboardString != null && !clipboardString.equals(previousStoredValue)) {
                        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
                        JSONObject clipboard = JSONParser.parseStrict(clipboardString).isObject();
                        JSONArray nodesArray = clipboard.get("nodes").isArray();
                        int size = nodesArray.size();
                        for (int i = 0; i < size; i++) {
                            nodes.add(getGwtJahiaNode(nodesArray.get(i).isObject()));
                        }
                        String type = clipboard.get("type").isString().stringValue();

                        if (!nodes.equals(copiedNodes) || cut != "cut".equals(type)) {
                            setClipboard(nodes, type);
                        }
                        previousStoredValue = clipboardString;
                    }
                }
            };

            t.schedule(100);
            t.scheduleRepeating(1000);
        }
    }

    private GWTJahiaNode getGwtJahiaNode(JSONObject object) {
        GWTJahiaNode node = new GWTJahiaNode();
        node.setUUID(object.get("uuid").isString().stringValue());
        node.setName(object.get("name").isString().stringValue());
        node.setPath(object.get("path").isString().stringValue());
        node.setDisplayName(object.get("displayName").isString().stringValue());
        node.setNodeTypes(getStringList(object.get("nodeTypes")));
        node.setInheritedNodeTypes(getStringList(object.get("inheritedNodeTypes")));

        JSONValue referenceNode = object.get("referenceNode");
        if (referenceNode != null && referenceNode.isObject() != null) {
            node.setReference(true);
            node.setReferencedNode(getGwtJahiaNode(referenceNode.isObject()));
        }
        return node;
    }

    private List<String> getStringList(JSONValue nodetypes) {
        List<String> l = new ArrayList<String>();
        JSONArray array = nodetypes.isArray();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                l.add(array.get(i).isString().stringValue());
            }
        }
        return l;
    }

    public void paste(final GWTJahiaNode m, final Linker linker, List<String> childNodeTypesToSkip, String newName) {
        if (!getCopiedNodes().isEmpty()) {
            JahiaContentManagementService
                    .App.getInstance().paste(JCRClientUtils.getPathesList(getCopiedNodes()), m.getPath(), newName, isCut(), childNodeTypesToSkip, new BaseAsyncCallback() {

                @Override
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

            @Override
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
            newPaths.add(target.getPath() + "/" + node.getName());
        }
        data.put("node", getCopiedNodes().get(0));
        data.put("nodes", newPaths);
        if (linker instanceof ManagerLinker) {
            ManagerLinker managerLinker = (ManagerLinker) linker;
            managerLinker.getLeftComponent().unmask();
            managerLinker.getTopRightComponent().unmask();
            data.put(Linker.REFRESH_ALL, true);
            linker.refresh(data);
        } else if (isCut() && MainModule.getInstance().getPath().contains(getCopiedNodes().get(0).getPath())) {
            MainModule.staticGoTo(newPaths.get(0), MainModule.getInstance().getTemplate());
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
            return that.@org.jahia.ajax.gwt.client.util.content.CopyPasteEngine::getCopiedNodesAsJs()();
        }
        $wnd.getJahiaClipboardIsCut = function () {
            return that.@org.jahia.ajax.gwt.client.util.content.CopyPasteEngine::isCut()();
        }
        $wnd.getJahiaClipboardClear = function () {
            that.@org.jahia.ajax.gwt.client.util.content.CopyPasteEngine::onPastedPath()();
        }
    }-*/;

    private JsArray<JavaScriptObject> getCopiedNodesAsJs() {
        List<JavaScriptObject> l = new ArrayList<JavaScriptObject>();
        for (GWTJahiaNode gwtJahiaNode : copiedNodes) {
            l.add(convertGwtNode(gwtJahiaNode));
        }
        return JsArrayUtils.readOnlyJsArray(l.toArray(new JavaScriptObject[l.size()]));
    }

    public static JsArrayString toJsArray(List<String> input) {
        JsArrayString jsArrayString = JsArrayString.createArray().cast();
        for (String s : input) {
            jsArrayString.push(s);
        }
        return jsArrayString;
    }

    private JavaScriptObject convertGwtNode(GWTJahiaNode gwtJahiaNode) {
        GWTJahiaNode ref = gwtJahiaNode.getReferencedNode();
        return convertGwtNode(gwtJahiaNode.getName(), gwtJahiaNode.getPath(), gwtJahiaNode.getUUID(),
                (gwtJahiaNode.getNodeTypes() != null && !gwtJahiaNode.getNodeTypes().isEmpty() ? gwtJahiaNode.getNodeTypes().get(0) : null), gwtJahiaNode.getDisplayName(),
                toJsArray(gwtJahiaNode.getNodeTypes()), toJsArray(gwtJahiaNode.getInheritedNodeTypes()), ref != null ? convertGwtNode(ref) : null);
    }

    private native JavaScriptObject convertGwtNode(String name, String path, String uuid, String nodeType, String displayName, JsArrayString nodeTypes, JsArrayString inheritedNodeTypes, JavaScriptObject referenceNode) /*-{
        return {
            'name': name,
            'path': path,
            'uuid': uuid,
            'nodetype': nodeType,
            'displayName': displayName,
            'nodeTypes': nodeTypes,
            'inheritedNodeTypes': inheritedNodeTypes,
            'referenceNode': referenceNode
        };
    }-*/;

    private native String stringify(JsArray<JavaScriptObject> nodes, String type) /*-{
        return JSON.stringify({'nodes': nodes, 'type': type});
    }-*/;

    private native void sendCopyEvent(JsArray<JavaScriptObject> nodes, String type)  /*-{
        $wnd.dispatchEvent(new CustomEvent('jahia-copy', {'detail': {'nodes': nodes, 'type': type}}));
    }-*/;

    public void setCopiedNodes(List<GWTJahiaNode> copiedPaths) {
        setClipboard(copiedPaths, "copy");
    }

    public void setCutNodes(List<GWTJahiaNode> cutPaths) {
        setClipboard(cutPaths, "cut");
    }

    public void onPastedPath() {
        setClipboard(Collections.<GWTJahiaNode>emptyList(), "copy");
    }

    private void setClipboard(List<GWTJahiaNode> copiedPaths, String type) {
        cut = "cut".equals(type);
        this.copiedNodes.clear();
        this.copiedNodes.addAll(copiedPaths);
        ClipboardActionItem.setCopied(copiedPaths);
        updatePlaceholders();

        JsArray<JavaScriptObject> copiedNodesAsJs = getCopiedNodesAsJs();
        if (storage != null) {
            storage.setItem("jahia-clipboard", stringify(copiedNodesAsJs, type));
        }
        sendCopyEvent(copiedNodesAsJs, type);
    }

    public boolean isCut() {
        return cut;
    }

    public boolean canCopyTo(GWTJahiaNode dest) {
        if (dest == null) {
            return false;
        }
        if (!copiedNodes.isEmpty()) {
            // check only first node ..
            GWTJahiaNode copiedPath = copiedNodes.get(0);
            return !(dest.getPath() + "/").startsWith(copiedPath.getPath() + "/") && (!isCut() || !copiedPath.getPath().substring(0, copiedPath.getPath().lastIndexOf('/')).equals(dest.getPath()));
        }
        return true;
    }

    /**
     * check if the provides node types match the type of the content in the clipboard.
     *
     * @param nodetypes       node types to check
     * @param checkReferences if true, checks also references.
     * @return true if the content in the clipboard match a type of the provided node types.
     */
    public boolean checkNodeType(String nodetypes, boolean checkReferences) {
        List<GWTJahiaNode> sources = getCopiedNodes();
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (!sources.isEmpty()) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    allowed &= isNodeAllowed(checkReferences, allowedTypes, source);
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

    private boolean isNodeAllowed(boolean checkReferences, String[] allowedTypes, GWTJahiaNode source) {
        for (String type : allowedTypes) {
            if ((source.isNodeType(type) || (source.isReference() && source.getReferencedNode() != null && source.getReferencedNode().isNodeType(type))) ||
                    (checkReferences && type.equals("jnt:contentReference") && source.isNodeType("jmix:droppableContent"))) {
                return true;
            }
        }
        return false;
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
