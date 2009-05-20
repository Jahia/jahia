/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.subscription;

import java.util.HashMap;
import java.util.Map;

import org.jahia.ajax.gwt.client.service.subscription.SubscriptionService;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionServiceAsync;
import org.jahia.ajax.gwt.client.messages.Messages;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Renders subscribe/unsubscribe button, depending on the user subscription
 * status.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscribeButton extends HTML {

    private static String getAttribute(Element elem, String attributeName,
            String defaultValue) {
        return elem.getAttribute(attributeName).length() > 0 ? elem
                .getAttribute(attributeName) : defaultValue;
    }

    private static String getMessage(String key) {
        String fullKey = "subscriptions.button." + key;
        return Messages.getNotEmptyResource(fullKey, fullKey);
    }

    private boolean confirmationRequired;

    private String event;

    private Map<String, String> i18n = new HashMap<String, String>();

    private String source;

    private SubscriptionStatus status = SubscriptionStatus.UNKNOWN;

    private Map<SubscriptionStatus, String> statusStyleMapping = new HashMap<SubscriptionStatus, String>(
            4);

    /**
     * Initializes an instance of this class.
     */
    public SubscribeButton(Element wrapper) {
        super();
        source = wrapper.getAttribute("source");
        event = wrapper.getAttribute("event");
        event = event.length() > 0 ? event : "contentPublished";
        confirmationRequired = "true".equals(wrapper
                .getAttribute("confirmationRequired"));
        initStyles(wrapper);
        initMessages(wrapper);
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
                        Window.alert(caught.getLocalizedMessage() + "\n"
                                + ex.toString());
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
                    if (Window.confirm(i18n.get("subscribe.confirm"))) {
                        subscribe();
                    }
                    break;

                case SUBSCRIBED:
                    if (Window.confirm(i18n.get("unsubscribe.confirm"))) {
                        unsubscribe();
                    }
                    break;

                case NO_EMAIL_ADDRESS:
                    Window.alert(i18n.get("provideEmailAddress"));
                    break;

                default:
                    break;
                }
            }
        });
        getStatus();
    }

    private void initMessages(Element elem) {
        i18n.put("operation.failure", getAttribute(elem,
                "messageOperationFailure", getMessage("operation.failure")));
        i18n.put("provideEmailAddress",
                getAttribute(elem, "messageProvideEmailAddress",
                        getMessage("provideEmailAddress")));
        i18n.put("subscribe.confirm",
                getAttribute(elem, "messageSubscribeConfirmation",
                        getMessage("subscribe.confirm")));
        i18n.put("subscribe.success", getAttribute(elem,
                "messageSubscribeSuccess", getMessage("subscribe.success")));
        i18n.put("subscribe.success.confirmationEmail", getAttribute(elem,
                "messageSubscribeSuccessConfirmationEmail",
                getMessage("subscribe.success.confirmationEmail")));
        i18n.put("subscribe.title", getAttribute(elem, "messageSubscribeTitle",
                getMessage("subscribe.title")));

        i18n.put("unsubscribe.confirm", getAttribute(elem,
                "messageUnsubscribeConfirmation",
                getMessage("unsubscribe.confirm")));

        i18n
                .put("unsubscribe.success", getAttribute(elem,
                        "messageUnsubscribeSuccess",
                        getMessage("unsubscribe.success")));

        i18n.put("unsubscribe.title", getAttribute(elem,
                "messageUnsubscribeTitle", getMessage("unsubscribe.title")));
    }

    private void initStyles(Element elem) {
        statusStyleMapping.put(SubscriptionStatus.UNAUTHORIZED, getAttribute(
                elem, "styleUnavailable",
                "subscribe-button subscription-unavailable"));
        statusStyleMapping.put(SubscriptionStatus.NO_EMAIL_ADDRESS,
                getAttribute(elem, "styleNoEmailAddress",
                        "subscribe-button subscription-unavailable"));
        statusStyleMapping.put(SubscriptionStatus.NOT_SUBSCRIBED, getAttribute(
                elem, "styleNotSubscribed", "subscribe-button not-subscribed"));
        statusStyleMapping.put(SubscriptionStatus.SUBSCRIBED, getAttribute(
                elem, "styleSubscribed", "subscribe-button subscribed"));
        statusStyleMapping.put(SubscriptionStatus.UNKNOWN, getAttribute(elem,
                "styleUnknown", "subscribe-button subscription-unknown"));
    }

    private void subscribe() {
        getService().subscribe(source, event, confirmationRequired,
                new AsyncCallback<SubscriptionStatus>() {
                    public void onFailure(Throwable caught) {
                        caught.printStackTrace();
                        Window.alert(i18n.get("operation.failure") + "\n"
                                + caught);
                    }

                    public void onSuccess(SubscriptionStatus result) {
                        if (SubscriptionStatus.SUBSCRIBED == result) {
                            Window
                                    .alert(i18n.get("subscribe.success")
                                            + (confirmationRequired ? " "
                                                    + i18n
                                                            .get("subscribe.success.confirmationEmail")
                                                    : ""));
                        } else {
                            Window.alert(i18n.get("operation.failure"));
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
                        Window.alert(i18n.get("operation.failure") + "\n"
                                + caught);
                    }

                    public void onSuccess(SubscriptionStatus result) {
                        if (SubscriptionStatus.NOT_SUBSCRIBED == result) {
                            Window.alert(i18n.get("unsubscribe.success"));
                        } else {
                            Window.alert(i18n.get("operation.failure"));
                        }
                        updateState(result);
                    }
                });
        updateState(SubscriptionStatus.NOT_SUBSCRIBED);
    }

    private void updateState(SubscriptionStatus status) {
        this.status = status;
        setStyleName(statusStyleMapping.get(status));
        String title = null;
        switch (status) {
        case SUBSCRIBED:
            title = i18n.get("unsubscribe.title");
            break;
        case NOT_SUBSCRIBED:
        case NO_EMAIL_ADDRESS:
            title = i18n.get("subscribe.title");
            break;

        default:
            break;
        }

        setTitle(title);
    }
}
