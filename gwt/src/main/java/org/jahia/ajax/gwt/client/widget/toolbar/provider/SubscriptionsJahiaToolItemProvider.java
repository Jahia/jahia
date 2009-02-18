/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import java.util.LinkedList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.ResourceBundle;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionService;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionInfo;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionServiceAsync;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionStatus;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Toolbar items provider for subscription services.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionsJahiaToolItemProvider extends
        AbstractJahiaToolItemProvider {

    private Button cancel;

    private LayoutContainer eventsContainer;

    private GWTJahiaPageContext pageContext;

    private FormPanel panel;

    private Button save;

    SubscriptionServiceAsync service = SubscriptionService.App.getInstance();

    private Window window;

    /**
     * Initializes an instance of this class.
     */
    public SubscriptionsJahiaToolItemProvider() {
        super();
        pageContext = this.getJahiaGWTPageContext();
    }

    @Override
    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

    @Override
    public SelectionListener<ComponentEvent> getSelectListener(
            final GWTJahiaToolbarItem gwtToolbarItem) {
        SelectionListener listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {

                List<SubscriptionInfo> subscriptions = new LinkedList<SubscriptionInfo>();
                String source = "ContentPage_" + pageContext.getPid();
                GWTJahiaProperty eventsProperty = gwtToolbarItem.getProperties()
                        .get("events");
                String[] events = eventsProperty != null
                        && eventsProperty.getValue() != null
                        && eventsProperty.getValue().length() > 0 ? eventsProperty
                        .getValue().split(",")
                        : new String[] { "contentPublished" };

                for (int i = 0; i < events.length; i++) {
                    subscriptions.add(new SubscriptionInfo(source, events[i]));
                }

                service.requestSubscriptionStatus(subscriptions,
                        new AsyncCallback<List<SubscriptionInfo>>() {
                            public void onFailure(Throwable caught) {
                                MessageBox mb = new MessageBox();
                                mb.setType(MessageBoxType.ALERT);
                                mb.setIcon(MessageBox.ERROR);
                                mb.setTitle("Error");
                                mb.setMessage(caught.toString());
                                mb.show();
                            }

                            public void onSuccess(List<SubscriptionInfo> result) {
                                showDialog(result);
                            }
                        });
            }
        };
        return listener;
    }

    private void showDialog(List<SubscriptionInfo> subscriptions) {

        window = new Window();
        window.setModal(true);
        window.setAutoHeight(true);
        window.setWidth(500);
        panel = new FormPanel();
        panel.setFrame(false);
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setButtonAlign(HorizontalAlignment.CENTER);
        panel.setLayout(new FlowLayout());

        eventsContainer = new LayoutContainer(new ColumnLayout());
        eventsContainer.setWidth(450);

        final LayoutContainer left = new LayoutContainer(new FlowLayout());

        for (SubscriptionInfo subscriptionInfo : subscriptions) {
            CheckBox cb = new CheckBox();
            cb.setBoxLabel(ResourceBundle.getNotEmptyResource(subscriptionInfo
                    .getEvent(), subscriptionInfo.getEvent()));
            cb.setName(subscriptionInfo.getEvent());
            cb
                    .setValue(subscriptionInfo.getStatus() == SubscriptionStatus.SUBSCRIBED);
            left.add(cb);
        }

        LayoutContainer right = new LayoutContainer(new FlowLayout());

        for (SubscriptionInfo subscriptionInfo : subscriptions) {
            CheckBox cb = new CheckBox();
            cb.setBoxLabel(ResourceBundle.getNotEmptyResource(
                    "includeChildren", "include child pages"));
            cb.setName(subscriptionInfo.getEvent() + "_includeChildren");
            cb.setValue(subscriptionInfo.isIncludeChildren());
            right.add(cb);
        }

        eventsContainer.add(left, new ColumnData(.60));
        eventsContainer.add(right, new ColumnData(.40));

        panel.add(eventsContainer);

        window.add(panel);
        window.setHeading(ResourceBundle.getNotEmptyResource(
                "subscriptions.windowTitle",
                "Subscribe to following events on the current page"));

        save = new Button(ResourceBundle.getNotEmptyResource("save", "Save"));
        save.addSelectionListener(new SelectionListener<ComponentEvent>() {

            public void componentSelected(ComponentEvent event) {
                updateSubscriptions();
            }
        });

        cancel = new Button(ResourceBundle.getNotEmptyResource("cancel",
                "Cancel"));
        cancel.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                window.close();
            }
        });

        panel.addButton(save);
        panel.addButton(cancel);

        window.recalculate();
        window.show();
    }

    private void updateSubscriptions() {
        List<SubscriptionInfo> subscriptions = new LinkedList<SubscriptionInfo>();
        String source = "ContentPage_" + pageContext.getPid();
        LayoutContainer leftColumn = (LayoutContainer) eventsContainer
                .getItem(0);
        LayoutContainer rightColumn = (LayoutContainer) eventsContainer
                .getItem(1);
        int count = leftColumn.getItemCount();
        for (int i = 0; i < count; i++) {
            CheckBox cbEvent = (CheckBox) leftColumn.getItem(i);
            CheckBox cbIncludeChildren = (CheckBox) rightColumn.getItem(i);
            subscriptions.add(new SubscriptionInfo(source, cbIncludeChildren
                    .getValue(), cbEvent.getName(),
                    cbEvent.getValue() ? SubscriptionStatus.SUBSCRIBED
                            : SubscriptionStatus.NOT_SUBSCRIBED));
        }

        service.updateSubscriptionStatus(subscriptions,
                new AsyncCallback<Boolean>() {
                    public void onFailure(Throwable caught) {
                        MessageBox mb = new MessageBox();
                        mb.setType(MessageBoxType.ALERT);
                        mb.setIcon(MessageBox.ERROR);
                        mb.setTitle("Error");
                        mb.setMessage(caught.toString());
                        mb.show();
                    }

                    public void onSuccess(Boolean success) {
                        MessageBox mb = new MessageBox();
                        mb.setType(MessageBoxType.ALERT);
                        mb
                                .setIcon(success ? MessageBox.INFO
                                        : MessageBox.ERROR);
                        mb.setTitle(success ? "Info" : "Error");
                        mb
                                .setMessage(success ? "Subscriptions updated sucessfully"
                                        : "Unable to update subscriptions status");
                        window.close();
                        mb.show();
                    }
                });
    }
}
