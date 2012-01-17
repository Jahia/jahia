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

package org.jahia.ajax.gwt.client.widget.subscription;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SendNewsletterActionItem extends BaseActionItem {

    @Override public void onComponentSelection() {
        new SendMailWindow(linker, linker.getSelectionContext().getSingleSelection()).show();
    }

    @Override public void handleNewLinkerSelection() {
        final GWTJahiaNode n = linker.getSelectionContext().getSingleSelection();
        setEnabled(n != null && n.getNodeTypes().contains("jnt:newsletterIssue") &&
                (n.getAggregatedPublicationInfo().getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED));
    }

    class SendMailWindow extends Window {

        private Linker m_linker;

        public SendMailWindow(final Linker linker, final GWTJahiaNode n) {
            super();

            m_linker = linker;

            setHeading(Messages.get("label.sendNewsletter", "Send newsletter issue"));
            setSize(500, 120);
            setResizable(false);
            setModal(true);
            ButtonBar buttons = new ButtonBar();

            final FormPanel form = new FormPanel();
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBorders(false);
            form.setBodyBorder(false);
            form.setLabelWidth(200);

            final CalendarField date = new CalendarField();
            date.setFieldLabel(Messages.get("label.scheduled", "Scheduled"));
            form.add(date);

            Button schedule = new Button(Messages.get("label.scheduleAsBackgroundJob"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                	if (date.getValue() == null) {
                		MessageBox.alert(Messages.get("label.sendNewsletter", "Send newsletter issue"), Messages.get("failure.invalid.date", "Please provide a valid date value"), null);
                		return;
                	}
                    mask();
                    doSchedule(date.getValue(), n);
                }
            });

            Button now = new Button(Messages.get("label.sendNow"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    mask();
                    doSend(n);
                }
            });

            Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            });

            buttons.add(schedule);
            buttons.add(now);
            buttons.add(cancel);

            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setBottomComponent(buttons);
            add(form);
        }

        private void doSend(GWTJahiaNode gwtJahiaNode) {
            Log.debug("action");
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(
                    JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/live/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading("Executing action ...");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + ".sendAsNewsletter.do");
            try {
                builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
                Request response = builder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        com.google.gwt.user.client.Window.alert("Cannot create connection");
                        linker.loaded();
                        unmask();
                        hide();
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() != 200) {
                            com.google.gwt.user.client.Window.alert("Cannot contact remote server : error "+response.getStatusCode());
                        }
                        linker.loaded();
                        unmask();
                        hide();
                        linker.refresh(EditLinker.REFRESH_MAIN);
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }

        }

        private void doSchedule(Date date, GWTJahiaNode gwtJahiaNode) {
            List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
            String propValueString = String.valueOf(date.getTime());
            props.add(new GWTJahiaNodeProperty("j:scheduled", new GWTJahiaNodePropertyValue(propValueString, GWTJahiaNodePropertyType.DATE)));

            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(gwtJahiaNode);
            JahiaContentManagementService.App.getInstance().saveProperties(nodes, props, null, new BaseAsyncCallback() {
                public void onSuccess(Object result) {
                    linker.loaded();
                    unmask();
                    hide();
                    linker.refresh(EditLinker.REFRESH_MAIN);
                }

                @Override public void onApplicationFailure(Throwable caught) {
                    super.onApplicationFailure(caught);
                    unmask();
                    hide();
                }
            });
        }
    }



}
