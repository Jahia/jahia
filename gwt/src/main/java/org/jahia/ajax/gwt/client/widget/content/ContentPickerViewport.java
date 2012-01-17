/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
public class ContentPickerViewport extends Viewport {
    private PickedContentView pickedContent;
    public static final int BUTTON_HEIGHT = 24;


    public ContentPickerViewport(final String jahiaContextPath, final String jahiaServletPath, final String filesServletPath,
                                 final String selectionLabel, final Map<String, String> selectorOptions, final List<GWTJahiaNode> selectedNodes,
                                 final List<String> filters, final List<String> mimeTypes, final GWTManagerConfiguration config,
                                 final boolean multiple, final String callback) {
        setLayout(new FitLayout());
        final ContentPicker picker =
                new ContentPicker(selectorOptions, selectedNodes, null, filters, mimeTypes, config, multiple);

        // buttom component
        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        Button ok = new Button(Messages.get("label.save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                List<String> selectedNode = getSelectedNodePathes(jahiaContextPath, jahiaServletPath, filesServletPath);
                if (selectedNode != null && !selectedNode.isEmpty()) {
                    callback(callback, selectedNode.get(0));
                    WindowUtil.close();
                }
            }
        });

        buttonBar.add(ok);

        if (selectedNodes == null || selectedNodes.size() ==0) {
            ok.setEnabled(false);
        }
        picker.setSaveButton(ok);

        Button cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                WindowUtil.close();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        final Component bar = buttonsPanel;
        picker.setBottomComponent(bar);

        add(picker);

        pickedContent = (PickedContentView) picker.getLinker().getBottomRightObject();
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
    public List<String> getSelectedNodePathes(final String jahiaContextPath, final String jahiaServletPath,
                                              final String filesServletPath) {
        return pickedContent.getSelectedContentPath(jahiaContextPath,jahiaServletPath, filesServletPath);
    }

    private native void callback(String callback, String url)/*-{

        $wnd.opener.CKEDITOR.tools.callFunction(callback, url,"");
      }-*/;


}