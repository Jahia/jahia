/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

/**
 * Action item to handle document editing via Google Docs.
 * 
 * @author Sergiy Shyrkov
 */
public class EditActionItem extends BaseActionItem {

    public static class EditWindow extends Window {

        private Linker linker;
        private GWTJahiaNode node;

        public EditWindow(final GWTJahiaNode node, Linker linker) {
            super();
            this.node = node;
            this.linker = linker;
        }

        @Override
        protected void onRender(Element parent, int pos) {
            super.onRender(parent, pos);
            setHeading(Messages.get("label.edit", "Edit") + " - " + node.getName());
            setSize(400, 200);
            setResizable(true);

            final ContentPanel panel = new ContentPanel();
            panel.setLayout(new RowLayout(Orientation.VERTICAL));
            panel.setHeaderVisible(false);
            panel.setBorders(false);
            panel.setBodyBorder(false);
            setModal(true);

            panel.add(new Label(Messages.get("label.googleDocs.edit.description",
                    "Please use the following button to open the document in Google Docs and start editing."
                            + "\nAfter editing is finished and the document is saved, please, "
                            + "click 'Stop editing' button to get the changes back into your local copy.")));

            panel.setButtonAlign(HorizontalAlignment.CENTER);

            final String msgStart = Messages.get("label.googleDocs.edit.start", "Start editing");
            final String msgStop = Messages.get("label.googleDocs.edit.stop", "Stop editing");

            final ToggleButton btnEdit = new ToggleButton(msgStart, StandardIconsProvider.STANDARD_ICONS.googleDocsLarge());
            btnEdit.setScale(ButtonScale.LARGE);
            btnEdit.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    if (btnEdit.isPressed()) {
                        com.google.gwt.user.client.Window.open(JahiaGWTParameters.getContextPath() + "/cms/gedit/"
                                + JahiaGWTParameters.getWorkspace() + node.getPath(), "_blank", "");
                        btnEdit.setText(msgStop);
                    } else {
                        panel.mask(Messages.get("label.synchronizing", "Synchronizing..."), "x-mask-loading");
                        JahiaContentManagementService.App.getInstance().synchronizeWithGoogleDocs(node.getUUID(),
                                new BaseAsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        panel.unmask();
                                        super.onFailure(caught);
                                        hide();
                                    }

                                    public void onSuccess(Void result) {
                                        panel.unmask();
                                        MessageBox.info(Messages.get("label.edit", "Edit"), Messages.get(
                                                "message.googleDocs.synchronize.success",
                                                "File content synchronized successfully."), null);
                                        hide();
                                        linker.refresh(Linker.REFRESH_MAIN);
                                    }
                                });
                    }
                }
            });
            panel.addButton(btnEdit);

            add(panel, new FlowData(10));
            setScrollMode(Style.Scroll.AUTO);
        }

    }

    private static final long serialVersionUID = 7067456022471038421L;

    private static void edit(Linker linker, GWTJahiaNode selection) {
        new EditWindow(selection, linker).show();
    }

    @Override
    public void handleNewLinkerSelection() {
        final LinkerSelectionContext lh = linker.getSelectionContext();
        final GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(singleSelection != null && PermissionsUtils.isPermitted("jcr:write", lh.getSelectionPermissions()) && lh.isFile());
    }

    @Override
    public void onComponentSelection() {
        edit(linker, linker.getSelectionContext().getSingleSelection());
    }

}
