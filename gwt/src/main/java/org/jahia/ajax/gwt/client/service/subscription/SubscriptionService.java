/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
