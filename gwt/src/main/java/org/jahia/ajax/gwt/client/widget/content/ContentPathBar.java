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

import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 28 ao�t 2008
 * Time: 13:30:11
 *
 * This component displays a path to the selected file.
 */
public class ContentPathBar extends TopBar {

    private ToolBar m_component ;
    private TextField selectedPath ;
    private String startPath ;
    private String callback ;
    private boolean allowThumbs;
    private ManagerConfiguration config;

    public ContentPathBar(final String startPath, final ManagerConfiguration config, final String callback, final boolean allowThumbs) {
        m_component = new ToolBar() ;
        selectedPath = new TextField() ;
        selectedPath.setId("file_id");
        selectedPath.setReadOnly(true);
        m_component.add(new LabelToolItem(Messages.getResource("fm_selection"))) ;
        m_component.add(selectedPath) ;

        selectedPath.setWidth(500);
        m_component.add(new FillToolItem()) ;
        Button deselect = new Button(Messages.getResource("fm_deselect")) ;
        deselect.setIconStyle("gxt-button-clear");
        deselect.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                getLinker().getTopRightObject().clearSelection() ;
                selectedPath.setRawValue("");
                if (callback != null && callback.length() > 0) {
                    nativeCallback(callback, selectedPath.getRawValue());
                }
            }
        });
        m_component.add(deselect) ;
        
        this.startPath = startPath ;
        this.config = config;
        this.callback = callback;
        this.allowThumbs = allowThumbs ;
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                initPathField(startPath);
            }
        });
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {
        List<GWTJahiaNode> selection = (List<GWTJahiaNode>) topTableSelection ;

        GWTJahiaNode node = selection.get(0);
        if (selection != null && selection.size() > 0 && (selection.get(0).getNodeTypes().contains(config.getNodeTypes()) || selection.get(0).getInheritedNodeTypes().contains(config.getNodeTypes()))) {
            final String path = selection.get(0).getPath();
            JahiaContentManagementService.App.getInstance().isFileAccessibleForCurrentContainer(path, new AsyncCallback<Boolean>() {
                public void onFailure(Throwable throwable) {
                    Log.error("unable to check ACLs");
                }
                public void onSuccess(Boolean accessible) {
                    boolean doIt = true;
                    if (!accessible.booleanValue()) {
                        doIt = Window.confirm("The file may not be readable by everyone, resulting in a broken link.\nDo you wish to continue ?");
                    }
                    if (doIt) {
                        selectedPath.setRawValue(path);
                        if (callback != null && callback.length() > 0) {
                            nativeCallback(callback, selectedPath.getRawValue());
                        }
                    }
                }
            });

        }
    }

    public Component getComponent() {
        return m_component ;
    }

    public void initPathField(String path) {
        selectedPath.setRawValue(path);
        selectedPath.setName("file_id");
    }

    public boolean isAllowThumbs() {
        return allowThumbs;
    }

    public String getCallback() {
        return callback;
    }

    public static native void nativeCallback(String callback, String path) /*-{
        try {
            eval('$wnd.' + callback)(path);
        } catch (e) {};
    }-*/;

}
