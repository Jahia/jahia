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

package org.jahia.ajax.gwt.client.widget.publication;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;

import java.util.List;

/**
 * Window, displaying the current publication status.
 * User: toto
 * Date: Jan 28, 2010
 * Time: 2:44:46 PM
 */
public class PublicationStatusWindow extends Window {
    protected Linker linker;
    protected Button ok;
    protected Button noWorkflow;
    protected Button cancel;
    protected boolean allSubTree;

    public PublicationStatusWindow(final Linker linker, final List<String> uuids, final List<GWTJahiaPublicationInfo> infos,
                            boolean allSubTree) {
        setLayout(new FitLayout());

        this.linker = linker;
        this.allSubTree = allSubTree;
        setScrollMode(Style.Scroll.NONE);
        setHeading("Publish");
        setSize(800, 500);
        setResizable(false);

        setModal(true);

        TableData d = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        d.setMargin(5);

        GroupingStore<GWTJahiaPublicationInfo> store = new GroupingStore<GWTJahiaPublicationInfo>();
        store.add(infos);

        final Grid<GWTJahiaPublicationInfo> grid = new PublicationStatusGrid(store);
        add(grid);

        cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });

        setButtonAlign(Style.HorizontalAlignment.CENTER);

        if (PermissionsUtils.isPermitted("edit-mode/publication", JahiaGWTParameters.getSiteKey())) {
            noWorkflow = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));
            noWorkflow.addSelectionListener(new ButtonEventSelectionListener(uuids));
            addButton(noWorkflow);
        }

        addButton(cancel);
    }

    private class ButtonEventSelectionListener extends SelectionListener<ButtonEvent> {
        private List<String> uuids;
        protected boolean workflow;

        public ButtonEventSelectionListener(List<String> uuids) {
            this.uuids = uuids;
        }

        public void componentSelected(ButtonEvent event) {
            ok.setEnabled(false);
            if (noWorkflow != null) {
                noWorkflow.setEnabled(false);
            }
            cancel.setEnabled(false);
            hide();
            final String status = Messages.get("label.publication.task", "Publishing content");
            Info.display(status,status);
            WorkInProgressActionItem.setStatus(status);
            JahiaContentManagementService.App.getInstance()
                    .publish(uuids, allSubTree, false, false, null, null, new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable caught) {
                            WorkInProgressActionItem.removeStatus(status);
                            Info.display("Cannot publish", "Cannot publish");
                            Log.error("Cannot publish", caught);
                        }

                        public void onSuccess(Object result) {
                            WorkInProgressActionItem.removeStatus(status);
                            Info.display(Messages.get("message.content.published"),
                                    Messages.get("message.content.published"));
                            linker.refresh(Linker.REFRESH_ALL);
                        }
                    });
        }
    }
}
