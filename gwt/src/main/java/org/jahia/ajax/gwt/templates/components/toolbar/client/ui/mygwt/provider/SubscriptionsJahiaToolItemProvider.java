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

package org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.provider;

import java.util.LinkedList;
import java.util.List;

import org.jahia.ajax.gwt.commons.client.util.ResourceBundle;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.templates.components.subscription.client.SubscriptionInfo;
import org.jahia.ajax.gwt.templates.components.subscription.client.SubscriptionService;
import org.jahia.ajax.gwt.templates.components.subscription.client.SubscriptionServiceAsync;
import org.jahia.ajax.gwt.templates.components.subscription.client.SubscriptionStatus;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;

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
 * TODO Comment me
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionsJahiaToolItemProvider extends
        AbstractJahiaToolItemProvider {

    private Button cancel;

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
    public ToolItem createNewToolItem(GWTToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

    @Override
    public SelectionListener<ComponentEvent> getSelectListener(
            final GWTToolbarItem gwtToolbarItem) {
        SelectionListener listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {

                List<SubscriptionInfo> subscriptions = new LinkedList<SubscriptionInfo>();
                subscriptions.add(new SubscriptionInfo("ContentPage_"
                        + pageContext.getPid(), "contentPublished"));

                service.requestSubscriptionStatus(subscriptions,
                        new AsyncCallback<List<SubscriptionInfo>>() {
                            public void onFailure(Throwable caught) {
                                MessageBox mb = new MessageBox();
                                mb.setType(MessageBoxType.ALERT);
                                mb.setIcon(MessageBox.ERROR);
                                mb.setTitle("Error");
                                mb.setMessage(caught.toString());
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

        LayoutContainer main = new LayoutContainer(new ColumnLayout());
        main.setWidth(400);

        final LayoutContainer left = new LayoutContainer(new FlowLayout());

        for (SubscriptionInfo subscriptionInfo : subscriptions) {
            CheckBox cb = new CheckBox();
            cb.setBoxLabel(ResourceBundle.getNotEmptyResource(subscriptionInfo
                    .getEvent(), "Be notified of "
                    + subscriptionInfo.getEvent() + " events"));
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

        main.add(left, new ColumnData(.65));
        main.add(right, new ColumnData(.35));

        panel.add(main);

        window.add(panel);
        window.setHeading(ResourceBundle.getNotEmptyResource(
                "subscriptions.windowTitle",
                "Subscribe to the following events on current page"));

        save = new Button(ResourceBundle.getNotEmptyResource("save", "Save"));
        save.addSelectionListener(new SelectionListener<ComponentEvent>() {

            public void componentSelected(ComponentEvent event) {
                MessageBox.alert("Saving...", "To be implemented soon...", null);
            }
        });

        cancel = new Button(ResourceBundle.getNotEmptyResource("cancel",
                "Cancel"));
        cancel.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                window.hide();
            }
        });

        panel.addButton(save);
        panel.addButton(cancel);

        window.recalculate();
        window.show();
    }
}
