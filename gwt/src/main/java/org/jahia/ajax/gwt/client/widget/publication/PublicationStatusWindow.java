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

package org.jahia.ajax.gwt.client.widget.publication;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;

import java.util.List;

/**
 * Window, displaying the current publication status.
 * User: toto
 * Date: Jan 28, 2010
 * Time: 2:44:46 PM
 */
public class PublicationStatusWindow extends LayoutContainer {
    protected Linker linker;
    protected Button noWorkflow;
    protected Button cancel;
    protected EngineContainer container;

    public PublicationStatusWindow(final Linker linker, final List<String> uuids, final List<GWTJahiaPublicationInfo> infos, final EngineContainer container) {
        setLayout(new FitLayout());

        this.linker = linker;
        setScrollMode(Style.Scroll.NONE);

        TableData d = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        d.setMargin(5);

        final Grid<GWTJahiaPublicationInfo> grid = new PublicationStatusGrid(infos, true, linker, container);
        add(grid);

        cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                container.closeEngine();
            }
        });

        ButtonBar bar = new ButtonBar();
        bar.setAlignment(Style.HorizontalAlignment.CENTER);

        if (uuids != null && infos != null && PermissionsUtils.isPermitted("publish", linker.getSelectionContext().getSingleSelection())) {
            noWorkflow = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));
            noWorkflow.addSelectionListener(new ButtonEventSelectionListener(uuids));
            bar.add(noWorkflow);
        }

        bar.add(cancel);

        this.container = container;
        container.setEngine(this, "Publish", bar, linker);
    }

    private class ButtonEventSelectionListener extends SelectionListener<ButtonEvent> {
        private List<String> uuids;
        protected boolean workflow;

        public ButtonEventSelectionListener(List<String> uuids) {
            this.uuids = uuids;
        }

        public void componentSelected(ButtonEvent event) {
            if (noWorkflow != null) {
                noWorkflow.setEnabled(false);
            }
            cancel.setEnabled(false);
            container.closeEngine();
            final String status = Messages.get("label.publication.task", "Publishing content");
            Info.display(status,status);
            WorkInProgressActionItem.setStatus(status);
            JahiaContentManagementService.App.getInstance()
                    .publish(uuids, null, null, new BaseAsyncCallback() {
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
