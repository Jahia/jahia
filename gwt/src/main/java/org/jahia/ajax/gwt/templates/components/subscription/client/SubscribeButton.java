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

package org.jahia.ajax.gwt.templates.components.subscription.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * SRenders subscribe/unsubscribe button, depending on the user subscription
 * status.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscribeButton extends HTML {

    private static final Map<SubscriptionStatus, String> STATUS_STYLE_MAPPING = new HashMap<SubscriptionStatus, String>(
            4);

    private static final String STYLE_SUBSCRIBE = "subscribe-button subscribe";

    private static final String STYLE_UNAVAILABLE = "subscribe-button subscription-unavailable";

    private static final String STYLE_UNKNOWN = "subscribe-button subscription-unknown";

    private static final String STYLE_UNSUBSCRIBE = "subscribe-button unsubscribe";
    //    
    static {
        STATUS_STYLE_MAPPING.put(SubscriptionStatus.UNAUTHORIZED, STYLE_UNAVAILABLE);
        STATUS_STYLE_MAPPING.put(SubscriptionStatus.NO_EMAIL_ADDRESS, STYLE_UNAVAILABLE);
        STATUS_STYLE_MAPPING.put(SubscriptionStatus.NOT_SUBSCRIBED,
                STYLE_SUBSCRIBE);
        STATUS_STYLE_MAPPING.put(SubscriptionStatus.SUBSCRIBED,
                STYLE_UNSUBSCRIBE);
        STATUS_STYLE_MAPPING.put(SubscriptionStatus.UNKNOWN, STYLE_UNKNOWN);
    }

    private boolean confirmationRequired;

    private String event;
    
    private String source;

    private SubscriptionStatus status = SubscriptionStatus.UNKNOWN;

    /**
     * Initializes an instance of this class.
     */
    public SubscribeButton(Element wrapper) {
        super();
        source = wrapper.getAttribute("source");
        event = wrapper.getAttribute("event");
        event = event.length() > 0 ? event : "contentPublished";
        confirmationRequired = "true".equals(wrapper.getAttribute("confirmationRequired"));
        init();
    }

    private SubscriptionServiceAsync getService() {
        return SubscriptionService.App.getInstance();
    }

    private void getStatus() {
        getService().getStatus(source, event,
                new AsyncCallback<SubscriptionStatus>() {
                    public void onFailure(Throwable caught) {
                        StringBuffer ex = new StringBuffer();
                        for (StackTraceElement element : caught.getStackTrace()) {
                            ex.append("\n").append(element);
                        }
                        Window.alert(caught.getLocalizedMessage() + "\n" + ex.toString());
                        updateState(SubscriptionStatus.UNKNOWN);
                    }

                    public void onSuccess(SubscriptionStatus result) {
                        updateState(result);
                    }
                });
    }

    private void init() {
        updateState(status);
        addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
                switch (status) {
                case NOT_SUBSCRIBED:
                    if (Window
                            .confirm("Would you like to be noified of changes in this content?")) {
                        subscribe();
                    }
                    break;

                case SUBSCRIBED:
                    if (Window
                            .confirm("Would you like to stop notifications about changes in this content?")) {
                        unsubscribe();
                    }
                    break;

                case NO_EMAIL_ADDRESS:
                    Window
                            .alert("You have not provided an e-mail address. Please update your profile settings first.");
                    break;

                default:
                    break;
                }
            }
        });
        getStatus();
    }

    private void subscribe() {
        getService().subscribe(source, event, confirmationRequired,
                new AsyncCallback<SubscriptionStatus>() {
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window
                                .alert("Unable to subscribe to the content change event. Cause:\n"
                                        + caught);
                    }

                    public void onSuccess(SubscriptionStatus result) {
                        if (SubscriptionStatus.SUBSCRIBED == result) {
                            Window
                                    .alert("Thank you for subscribing to our Notification Service!"
                                            + (confirmationRequired ? " An e-mail will be sent to your address for confirmation."
                                                    : ""));
                        } else {
                            Window
                                    .alert("Unable to subscribe to the content change event.");
                        }
                        updateState(result);
                    }
                });
    }

    private void unsubscribe() {
        getService().unsubscribe(source, event,
                new AsyncCallback<SubscriptionStatus>() {
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window
                                .alert("Unable to complete unsubscribe request to the content change event. Cause:\n"
                                        + caught);
                    }

                    public void onSuccess(SubscriptionStatus result) {
                        if (SubscriptionStatus.NOT_SUBSCRIBED == result) {
                            Window.alert("Your unsubscription request was successful.");
                        } else {
                            Window
                                    .alert("Unable to complete unsubscribe request to the content change event.");
                        }
                        updateState(result);
                    }
                });
        updateState(SubscriptionStatus.NOT_SUBSCRIBED);
    }

    private void updateState(SubscriptionStatus status) {
        this.status = status;
        setStyleName(STATUS_STYLE_MAPPING.get(status));
        String title = null;
        switch (status) {
        case SUBSCRIBED:
            title = "Unsubscribe from being notified of changes for this content";
            break;
        case NOT_SUBSCRIBED:
        case NO_EMAIL_ADDRESS:
            title = "Subscribe to be notified of changes for this content";
            break;

        default:
            break;
        }
        
        setTitle(title);
    }
}
