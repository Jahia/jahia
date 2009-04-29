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
package org.jahia.ajax.gwt.client.service.subscription;

import java.util.List;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionInfo;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Subscription service definition.
 * 
 * @author Sergiy Shyrkov
 */
public interface SubscriptionService extends RemoteService {

    public static class App {
        private static SubscriptionServiceAsync app = null;

        public static synchronized SubscriptionServiceAsync getInstance() {
            if (app == null) {
                app = GWT.create(SubscriptionService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(URL
                        .getAbsolutleURL(JahiaGWTParameters
                                .getServiceEntryPoint()
                                + "subscription/"));
            }
            return app;
        }
    }

    /**
     * Returns the status information about user subscription.
     * 
     * @param objectKey
     *            the content object key
     * @param eventType
     *            the event type
     * @return the status information about user subscription
     */
    SubscriptionStatus getStatus(String objectKey, String eventType);

    /**
     * Returns the status of the specified user subscriptions.
     * 
     * @param subscriptions
     *            list of subscriptions to check the status the event type
     * @return the status of the specified user subscriptions
     */
    List<SubscriptionInfo> requestSubscriptionStatus(
            List<SubscriptionInfo> subscriptions);

    /**
     * Creates a subscription entry for the current user, the specified object
     * key and event type. If the <code>confirmationRequired</code> is set to
     * <code>true</code> an e-mail with the confirmation request will be sent.
     * 
     * @param objectKey
     *            the content object key
     * @param eventType
     *            the event type
     * @param confirmationRequired
     *            if set to <code>true</code> an e-mail with the confirmation
     *            request will be sent
     * @return the status information about user subscription
     */
    SubscriptionStatus subscribe(String objectKey, String eventType,
            boolean confirmationRequired);

    /**
     * Removes a subscription entry for the current user, the specified object
     * key and event type.
     * 
     * @param objectKey
     *            the content object key
     * @param eventType
     *            the event type
     * @return the status information about user subscription
     */
    SubscriptionStatus unsubscribe(String objectKey, String eventType);

    /**
     * Updates the status for the specified user subscriptions.
     * 
     * @param subscriptions
     *            the user subscriptions to be updated
     * @return the result of the operation
     */
    Boolean updateSubscriptionStatus(List<SubscriptionInfo> subscriptions);
}
