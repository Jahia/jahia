package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.*;
import java.util.regex.Matcher;

public class NewPackageActionItem extends NodeTypeAwareBaseActionItem {


    private static final long serialVersionUID = -2760798544350502175L;

    public void onComponentSelection() {
        GWTJahiaNode parent = linker.getSelectionContext().getSingleSelection();
        GWTJahiaNode newNode = null;
        if (parent != null) {
            final String nodeName = Window.prompt(Messages.get("label.newJavaPackage"), "untitled");
            if (nodeName != null && nodeName.length() > 0 && !nodeName.matches("[A-Z]*")) {
                linker.loading("");
                List<String> packages = Arrays.asList(nodeName.split("\\."));
                Collections.reverse(packages);
                for (String packageName : packages) {
                    GWTJahiaNode parentNode = new GWTJahiaNode();
                    parentNode.setName(packageName);
                    parentNode.setNodeTypes(Arrays.asList("jnt:folder"));
                    if (newNode != null) {
                        parentNode.add(newNode);
                    }
                    newNode = parentNode;
                }
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), newNode, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode node) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                        linker.loaded();
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("node", node);
                        linker.refresh(data);
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        Log.error("Unable to create [" + nodeName + "]", throwable);
                        linker.loaded();
                    }
                });
            }
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null
                && !lh.isLocked()
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions())
                && isNodeTypeAllowed(lh.getSingleSelection()));
    }
}


