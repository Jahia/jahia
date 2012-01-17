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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseLanguageAwareActionItem;

import java.util.List;

public class TestNewsletterActionItem extends BaseLanguageAwareActionItem {

    @Override public void onComponentSelection() {
        new EmailTestWindow(linker.getSelectionContext().getSingleSelection()).show();
    }

    @Override public void handleNewLinkerSelection() {
        final GWTJahiaNode n = linker.getSelectionContext().getSingleSelection();
        setEnabled(n != null && n.getNodeTypes().contains("jnt:newsletterIssue") &&
                (n.getAggregatedPublicationInfo().getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED));
    }

    class EmailTestWindow extends Window {

        public EmailTestWindow(final GWTJahiaNode n) {
            super();
            setLayout(new FitLayout());

            setHeading(Messages.get("label.testNewsletter", "Test newsletter issue"));
            setSize(500, 170);
            setResizable(false);
            setModal(true);
            ButtonBar buttons = new ButtonBar();

            final FormPanel form = new FormPanel();
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBorders(false);
            form.setBodyBorder(false);
            form.setLabelWidth(200);

            final TextField<String> mail = new TextField<String>();
            mail.setFieldLabel(Messages.get("label.email", "Email"));
            mail.setName("testemail");
            mail.setAllowBlank(false);
            form.add(mail);

            final TextField<String> user = new TextField<String>();
            user.setFieldLabel(Messages.get("label.user", "User"));
            user.setName("user");
            user.setValue("guest");
            form.add(user);

            final ComboBox<GWTJahiaLanguage> locale = new ComboBox<GWTJahiaLanguage>();
            locale.setStore(new ListStore<GWTJahiaLanguage>());
            locale.getStore().add(JahiaGWTParameters.getSiteLanguages());
            locale.setDisplayField("displayName");
            locale.setTypeAhead(true);
            locale.setTriggerAction(ComboBox.TriggerAction.ALL);
            locale.setForceSelection(true);
            locale.setEditable(false);
            locale.setFieldLabel(Messages.get("label.language", "Language"));
            locale.setValue(selectedLang);
            locale.setName("locale");
            form.add(locale);

            Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                	if (mail.getValue() == null || mail.getValue().trim().length() == 0) {
                		MessageBox.alert(Messages.get("label.testNewsletter", "Test newsletter issue"), Messages.get("failure.invalid.emailAddress", "Please enter valid e-mail address"), null);
                		return;
                	}
                    mask();
                    String data = "testemail=" + URL.encodeQueryString(mail.getValue());
                    data += "&type=html&user=" + (user.getValue() != null ? URL.encodeQueryString(user.getValue()) : "");
                    data += "&locale=" + URL.encodeQueryString(locale.getValue().getLanguage());
                    doAction(linker.getSelectionContext().getSingleSelection(), data);
                }
            });

            Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            });

            buttons.add(submit);
            buttons.add(cancel);
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setBottomComponent(buttons);
            add(form);
        }

        public void doAction(GWTJahiaNode gwtJahiaNode, final String requestData) {
            Log.debug("action");
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(
                                JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading("Executing action ...");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + ".sendAsNewsletter.do");
            try {
                builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
                builder.sendRequest(requestData, new RequestCallback() {
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
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }

        }


    }
}
