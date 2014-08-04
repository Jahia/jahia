/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected boolean unpublish;
    protected EngineContainer container;
    protected List<String> uuids;

    public PublicationStatusWindow(final Linker linker, final List<String> uuids, final List<GWTJahiaPublicationInfo> infos, final EngineContainer container, boolean unpublish) {
        setLayout(new FitLayout());

        this.linker = linker;
        this.unpublish = unpublish;
        this.uuids = uuids;
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

        String language = null;
        if (uuids != null && infos != null && !infos.isEmpty() && infos.get(0).isAllowedToPublishWithoutWorkflow()) {
            noWorkflow = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));
            noWorkflow.addSelectionListener(new ButtonEventSelectionListener(uuids));
            language = infos.get(0).getWorkflowGroup().substring(0,infos.get(0).getWorkflowGroup().indexOf(" "));
            bar.add(noWorkflow);
        }

        bar.add(cancel);

        this.container = container;
        container.setEngine(this, "Publish", bar, JahiaGWTParameters.getLanguage(language), linker);
    }

    public List<String> getUuids() {
        return uuids;
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
            if (unpublish) {
                final String status = Messages.get("label.publication.unpublished.task", "Unpublishing content");
                Info.display(status, status);
                WorkInProgressActionItem.setStatus(status);
                JahiaContentManagementService.App.getInstance()
                        .unpublish(uuids, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                WorkInProgressActionItem.removeStatus(status);
                                Info.display("Cannot unpublish", "Cannot unpublish");
                                Log.error("Cannot unpublish", caught);
                            }

                            public void onSuccess(Object result) {
                                WorkInProgressActionItem.removeStatus(status);
                                Info.display(Messages.get("label.publication.unpublished"),
                                        Messages.get("label.publication.unpublished"));
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_ALL, true);
                                linker.refresh(data);
                            }
                        });
            } else {
                final String status = Messages.get("label.publication.task", "Publishing content");
                Info.display(status, status);
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
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_ALL, true);
                                linker.refresh(data);
                            }
                        });
            }
        }
    }
}
