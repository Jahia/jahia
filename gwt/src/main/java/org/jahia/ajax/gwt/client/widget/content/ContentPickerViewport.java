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

import org.jahia.ajax.gwt.client.data.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

/**
 * File and folder picker control.
 *
 * @author rfelden
 *         Date: 27 ao�t 2008
 */
public class ContentPickerViewport extends TriPanelBrowserViewport {
    private PickedContent pickedContent;
    public static final int BUTTON_HEIGHT = 24;


    public ContentPickerViewport(final String jahiaContextPath,final String jahiaServletPath,final  String selectionLabel, final String rootPath,final  Map<String, String> selectorOptions, final List<GWTJahiaNode> selectedNodes,final  String types,final  String filters,final  String mimeTypes,final GWTManagerConfiguration managerConfiguration,final  boolean multiple,final  boolean allowThumbs, String callback) {
        super();

        if (types != null && types.length() > 0) {
            managerConfiguration.setNodeTypes(types);
        }
        if (mimeTypes != null && mimeTypes.length() > 0) {
            managerConfiguration.setMimeTypes(mimeTypes);
        }
        if (filters != null && filters.length() > 0) {
            managerConfiguration.setFilters(filters);
        }

        // construction of the UI components
        boolean linkPicker = managerConfiguration.getName().equalsIgnoreCase(ManagerConfigurationFactory.LINKPICKER);
        final BottomRightComponent bottomComponents;
        if (linkPicker) {
            bottomComponents = new PickedPageView(managerConfiguration.getName(), false, true, selectedNodes, multiple, managerConfiguration, true);
        } else {
            bottomComponents = new PickedContentView(selectionLabel, managerConfiguration.getName(), selectedNodes, multiple, managerConfiguration);
        }

        // top right componet
        final TopRightComponent contentPicker = new ContentPickerBrowser(managerConfiguration.getName(), rootPath, selectedNodes, managerConfiguration, multiple);

        // buttom component
        final Component bar = initButtonBar(jahiaContextPath,jahiaServletPath,callback);

        if (linkPicker) {
            setCenterData(new BorderLayoutData(Style.LayoutRegion.SOUTH, 500));
            initWidgets(null, bottomComponents.getComponent(), contentPicker.getComponent(), null, bar);
        } else {
            initWidgets(null, contentPicker.getComponent(), bottomComponents.getComponent(), null, bar);
        }

        // linker initializations
        linker.registerComponents(null, contentPicker, bottomComponents, null, null);
        contentPicker.initContextMenu();
        linker.handleNewSelection();

        pickedContent = (PickedContent) bottomComponents;
    }

    /**
     * Get selected node
     *
     * @return
     */
    public List<GWTJahiaNode> getSelectedNodes() {
        return pickedContent.getSelectedContent();
    }

    /**
     * Get selectedNode pathes
     *
     * @return
     */
    public List<String> getSelectedNodePathes(final String jahiaContextPath,final String jahiaServletPath) {
        return pickedContent.getSelectedContentPath(jahiaContextPath,jahiaServletPath,true);
    }

    /**
     * Init buttonBar
     *
     * @return
     */
    private Component initButtonBar(final String jahiaContextPath,final String jahiaServletPath, final String callback) {
        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        Button ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                List<String> selectedNode = getSelectedNodePathes(jahiaContextPath,jahiaServletPath);
                if (selectedNode != null && !selectedNode.isEmpty()) {
                    callback(callback, selectedNode.get(0));
                    WindowUtil.close();
                }
            }
        });

        buttonBar.add(ok);


        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                WindowUtil.close();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        // copyrigths
        Text copyright = new Text(Messages.getResource("fm_copyright"));
        ButtonBar container = new ButtonBar();
        container.setAlignment(Style.HorizontalAlignment.CENTER);
        container.add(copyright);
        buttonsPanel.add(container);

        return buttonsPanel;
    }

    private native void callback(String callback, String url)/*-{

        $wnd.opener.CKEDITOR.tools.callFunction(callback, url,"");
      }-*/;


}