/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 28 ao�t 2008
 * Time: 13:30:11
 *
 * This component displays a path to the selected file.
 */
public class FilePathBar extends TopBar {

    private ToolBar m_component ;
    private TextField selectedPath ;
    private String startPath ;
    private String callback ;
    private boolean allowThumbs;
    private ManagerConfiguration config;

    public FilePathBar(final String startPath, final ManagerConfiguration config, final String callback, final boolean allowThumbs) {
        m_component = new ToolBar() ;
        selectedPath = new TextField() ;
        selectedPath.setId("file_id");
        selectedPath.setReadOnly(true);
        m_component.add(new LabelToolItem(Messages.getResource("fm_selection"))) ;
        m_component.add(new AdapterToolItem(selectedPath)) ;

        selectedPath.setWidth(500);
        m_component.add(new FillToolItem()) ;
        TextToolItem deselect = new TextToolItem(Messages.getResource("fm_deselect")) ;
        deselect.setIconStyle("gxt-button-clear");
        deselect.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
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
        
        if (selection != null && selection.size() > 0 && (selection.get(0).getNodeTypes().contains(config.getNodeTypes()) || selection.get(0).getInheritedNodeTypes().contains(config.getNodeTypes()))) {
            selectedPath.setRawValue(selection.get(0).getPath());
            if (callback != null && callback.length() > 0) {
                nativeCallback(callback, selectedPath.getRawValue());
            }
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
