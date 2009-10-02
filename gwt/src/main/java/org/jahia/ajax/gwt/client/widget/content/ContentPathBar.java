/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;

import java.util.List;

/**
 * This component displays a path to the selected file.
 * 
 * User: rfelden
 * Date: 28 ao�t 2008
 * Time: 13:30:11
 */
public class ContentPathBar extends TopBar {

    private ToolBar m_component ;
    private TextField<String> selectedPath ;
    private TextField<String> selectedUuid ;
    List<GWTJahiaNode> selectedNodes ;
    private String callback ;
    private boolean allowThumbs;
    private ManagerConfiguration config;

    public ContentPathBar(List<GWTJahiaNode> selectedNodes, final ManagerConfiguration config, final String callback, final boolean allowThumbs) {
        m_component = new ToolBar() ;
        selectedPath = new TextField<String>() ;
        selectedPath.setId("content_id");
        selectedPath.setReadOnly(true);
        m_component.add(new LabelToolItem(Messages.getResource("fm_selection"))) ;
        m_component.add(selectedPath) ;

        selectedPath.setWidth(500);
        
        selectedUuid = new TextField<String>();
        selectedUuid.setId("content_uuid");
        selectedUuid.setVisible(false);
        m_component.add(selectedUuid);
        
        m_component.add(new FillToolItem()) ;
        Button deselect = new Button(Messages.getResource("fm_deselect")) ;
        deselect.setIconStyle("gxt-button-clear");
        deselect.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                getLinker().getTopRightObject().clearSelection() ;
                selectedPath.setRawValue("");
                selectedUuid.setRawValue("");
                if (callback != null && callback.length() > 0) {
                    nativeCallback(callback, selectedPath.getRawValue(), selectedUuid.getRawValue());
                }
            }
        });
        m_component.add(deselect) ;
        
        this.selectedNodes = selectedNodes;
        this.config = config;
        this.callback = callback;
        this.allowThumbs = allowThumbs ;
    }

    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                if (selectedNodes.isEmpty()) {
                    initPathField("", "");
                } else {
                    initPathField(selectedNodes.get(0).getPath(), selectedNodes.get(0).getUUID());
                }
            }
        });
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {
        List<GWTJahiaNode> selection = (List<GWTJahiaNode>)getLinker().getSelectionContext().getSelectedNodes();// (List<GWTJahiaNode>) topTableSelection ;
        String[] n = config.getNodeTypes().split(",");
        if (selection != null && selection.size() > 0) {
            boolean found = false;
            for (String s : n) {
                if ((selection.get(0).getNodeTypes().contains(s) || selection.get(0).getInheritedNodeTypes().contains(s))) {
                    found = true;
                    break;
                }                
            }
            if (found) {
            final GWTJahiaNode selectedNode = selection.get(0);
            JahiaContentManagementService.App.getInstance().isFileAccessibleForCurrentContainer(selectedNode.getPath(), new AsyncCallback<Boolean>() {
                public void onFailure(Throwable throwable) {
                    Log.error("unable to check ACLs");
                }
                public void onSuccess(Boolean accessible) {
                    boolean doIt = true;
                    if (!accessible.booleanValue()) {
                        doIt = Window.confirm("The file may not be readable by everyone, resulting in a broken link.\nDo you wish to continue ?");
                    }
                    if (doIt) {
                        selectedPath.setRawValue(selectedNode.getPath());
                        selectedUuid.setRawValue(selectedNode.getUUID());
                        if (callback != null && callback.length() > 0) {
                            nativeCallback(callback, selectedPath.getRawValue(), selectedUuid.getRawValue());
                        }
                    }
                }
            });
            }

        }
    }

    public Component getComponent() {
        return m_component ;
    }

    public void initPathField(String path, String uuid) {
        selectedPath.setRawValue(path);
        selectedPath.setName("content_id");
        selectedUuid.setRawValue(uuid);
        selectedUuid.setName("content_uuid");
    }

    public boolean isAllowThumbs() {
        return allowThumbs;
    }

    public String getCallback() {
        return callback;
    }

    public static native void nativeCallback(String callback, String path, String uuid) /*-{
        try {
            eval('$wnd.' + callback)(path, uuid);
        } catch (e) {};
    }-*/;

}
