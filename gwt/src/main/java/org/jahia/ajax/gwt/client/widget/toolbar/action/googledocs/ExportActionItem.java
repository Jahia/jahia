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

package org.jahia.ajax.gwt.client.widget.toolbar.action.googledocs;

import java.util.List;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.FileIconsImageBundle;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;

/**
 * Action item to handle document export into different formats using Google
 * Docs API.
 * 
 * @author Sergiy Shyrkov
 */
public class ExportActionItem extends BaseActionItem {

    public static class ExportWindow extends Window {

        private GWTJahiaNode node;
        
        public ExportWindow(final GWTJahiaNode node) {
            super();
            this.node = node;
        }
        
        @Override
        protected void onRender(Element parent, int pos) {
            super.onRender(parent, pos);
            setHeading(Messages.get("label.export", "Export") + " - " + node.getName());
            setSize(400, 370);
            setResizable(true);

            final ContentPanel panel = new ContentPanel();
            panel.setLayout(new RowLayout(Orientation.VERTICAL));
            panel.setHeaderVisible(false);
            panel.setBorders(false);
            panel.setBodyBorder(false);
            setModal(true);

            panel.add(new Label(Messages.get("label.googleDocs.export.description",
                    "Here you can find possible output formats" + " by selecting a corresponding link") + ":"));

            final ContentPanel buttonPanel = new ContentPanel();
            buttonPanel.setHeight(220);
            buttonPanel.setLayout(new RowLayout(Orientation.VERTICAL));
            buttonPanel.setHeaderVisible(false);
            buttonPanel.setBorders(false);
            buttonPanel.setBodyBorder(false);
            
            panel.add(buttonPanel, new FlowData(10, 0, 0, 100));
            
            final String basePath = JahiaGWTParameters.getContextPath() + "/cms/gconvert/"
                    + JahiaGWTParameters.getWorkspace() + node.getPath() + "?exportFormat=";
            
            panel.mask(Messages.get("label_loading","Loading..."));
            JahiaContentManagementService.App.getInstance().getGoogleDocsExportFormats(node.getUUID(), new BaseAsyncCallback<List<String>>() {

                public void onSuccess(List<String> result) {
                    panel.unmask();
                    for (final String format : result) {
                        Button btn = new Button(Messages.get("label.googleDocs.export.format." + format, format), FileIconsImageBundle.Provider.get(format));
                        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                com.google.gwt.user.client.Window.open(basePath + format, "_blank", "");
                            }
                        });
                        buttonPanel.add(btn, new RowData(-1, -1, new Margins(2)));
                    }
                    layout();
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    panel.unmask();
                    super.onFailure(caught);
                }
            });
            
            panel.add(new Label(Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer",
            "Disclaimer") + ": " + Messages.get("label.googleDocs.export.disclaimer", "")));

            panel.setButtonAlign(HorizontalAlignment.CENTER);
            panel.addButton(new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            }));

            add(panel, new FlowData(10));
            setScrollMode(Style.Scroll.AUTO);
        }

    }

    private static final long serialVersionUID = 7067456022471038421L;

    private static void export(Linker linker, GWTJahiaNode selection) {
        linker.loading(Messages
                .get("org.jahia.engines.filemanager.Filemanager_Engine.statusbar.downloading.label"));
        new ExportWindow(selection).show();
        linker.loaded();
    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isSingleFile());
    }

    @Override
    public void onComponentSelection() {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            if (selection != null && selection.isFile()) {
                export(linker, selection);
            }
        }
    }

}
