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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionStatus;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionInfo;

/**
 * Subscription service definition.
 * 
 * @author Sergiy Shyrkov
 */
public interface SubscriptionServiceAsync {

    void getStatus(String objectKey, String eventType,
            AsyncCallback<SubscriptionStatus> callback);

    void requestSubscriptionStatus(List<SubscriptionInfo> subscriptions,
            AsyncCallback<List<SubscriptionInfo>> callback);

    void subscribe(String objectKey, String eventType,
            boolean confirmationRequired,
            AsyncCallback<SubscriptionStatus> callback);

    void unsubscribe(String objectKey, String eventType,
            AsyncCallback<SubscriptionStatus> callback);

    void updateSubscriptionStatus(List<SubscriptionInfo> subscriptions,
            AsyncCallback<Boolean> callback);

}