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
package org.jahia.ajax.gwt.templates.components.subscription.server;

import java.util.List;

import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionInfo;
import org.jahia.ajax.gwt.client.service.subscription.SubscriptionService;
import org.jahia.ajax.gwt.client.widget.subscription.SubscriptionStatus;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.mail.MailHelper;
import org.jahia.services.notification.Subscription;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;

/**
 * GWT subscription service implementation.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionServiceImpl extends AbstractJahiaGWTServiceImpl
        implements SubscriptionService {

    private static org.jahia.services.notification.SubscriptionService getSubscriptionService() {
        return ServicesRegistry.getInstance().getSubscriptionService();
    }

    private int getSiteId() {
        ProcessingContext ctx = retrieveParamBean();
        int siteId = ctx.getSiteID();
        if (SettingsBean.getInstance().isDevelopmentMode() && siteId <= 0) {
            String siteIdParameter = getThreadLocalRequest().getParameter(
                    "site");
            if (siteIdParameter != null) {
                siteId = Integer.parseInt(siteIdParameter);
            }
        }
        return siteId;
    }

    public SubscriptionStatus getStatus(String objectKey, String eventType) {
        SubscriptionStatus status = SubscriptionStatus.UNKNOWN;
        JahiaUser user = getUser();
        if (JahiaUserManagerService.isGuest(user)) {
            status = SubscriptionStatus.UNAUTHORIZED;
        } else if (MailHelper.getEmailAddress(user) == null) {
            status = SubscriptionStatus.NO_EMAIL_ADDRESS;
        } else {
            status = getSubscriptionService().isSubscribed(objectKey,
                    eventType, user.getUsername(), getSiteId()) ? SubscriptionStatus.SUBSCRIBED
                    : SubscriptionStatus.NOT_SUBSCRIBED;
        }

        return status;
    }

    private JahiaUser getUser() {
        ProcessingContext ctx = retrieveParamBean();
        JahiaUser user = ctx.getUser();
        if (SettingsBean.getInstance().isDevelopmentMode()
                && JahiaUserManagerService.isGuest(user)) {
            String userName = getThreadLocalRequest().getParameter("user");
            if (userName != null) {
                user = ServicesRegistry.getInstance()
                        .getJahiaUserManagerService().lookupUser(userName);
            }
        }
        return user;
    }

    public List<SubscriptionInfo> requestSubscriptionStatus(
            List<SubscriptionInfo> subscriptions) {

        JahiaUser user = getUser();
        if (JahiaUserManagerService.isGuest(user)) {
            for (SubscriptionInfo subscription : subscriptions) {
                subscription.setStatus(SubscriptionStatus.UNAUTHORIZED);
            }
        } else {
            org.jahia.services.notification.SubscriptionService subscriptionService = getSubscriptionService();
            for (SubscriptionInfo subscription : subscriptions) {
                Subscription subscriptionData = subscriptionService
                        .getSubscription(subscription.getSource(), subscription
                                .getEvent(), user.getUsername(), getSiteId());
                if (subscriptionData != null) {
                    subscription.setStatus(SubscriptionStatus.SUBSCRIBED);
                    subscription.setIncludeChildren(subscriptionData
                            .isIncludeChildren());
                } else {
                    subscription.setStatus(SubscriptionStatus.NOT_SUBSCRIBED);
                }
            }
        }
        return subscriptions;
    }

    public SubscriptionStatus subscribe(String objectKey, String eventType,
            boolean confirmationRequired) {

        SubscriptionStatus status = SubscriptionStatus.SUBSCRIBED;

        org.jahia.services.notification.SubscriptionService service = ServicesRegistry
                .getInstance().getSubscriptionService();

        JahiaUser user = getUser();
        if (JahiaUserManagerService.isGuest(user)) {
            status = SubscriptionStatus.UNAUTHORIZED;
        } else {
            if (confirmationRequired) {
                service.subscribeAndAskForConfirmation(objectKey, true,
                        eventType, user.getUsername(), getSiteId());
            } else {
                service.subscribe(objectKey, true, eventType, user
                        .getUsername(), getSiteId());
            }
        }
        return status;
    }

    public SubscriptionStatus unsubscribe(String objectKey, String eventType) {

        SubscriptionStatus status = SubscriptionStatus.NOT_SUBSCRIBED;

        org.jahia.services.notification.SubscriptionService service = ServicesRegistry
                .getInstance().getSubscriptionService();

        ProcessingContext ctx = retrieveParamBean();
        if (JahiaUserManagerService.isGuest(ctx.getUser())) {
            status = SubscriptionStatus.UNAUTHORIZED;
        } else {
            service.unsubscribe(objectKey, eventType, ctx.getUser()
                    .getUsername(), ctx.getSiteID());
            status = getStatus(objectKey, eventType);
        }
        return status;
    }

    public Boolean updateSubscriptionStatus(List<SubscriptionInfo> subscriptions) {
        JahiaUser user = getUser();
        if (JahiaUserManagerService.isGuest(user)) {
            return false;
        } else {
            org.jahia.services.notification.SubscriptionService subscriptionService = getSubscriptionService();
            for (SubscriptionInfo subscription : subscriptions) {
                if (SubscriptionStatus.SUBSCRIBED == subscription.getStatus()) {
                    subscriptionService.subscribe(subscription.getSource(),
                            subscription.isIncludeChildren(), subscription
                                    .getEvent(), user.getUsername(),
                            getSiteId());
                } else if (SubscriptionStatus.NOT_SUBSCRIBED == subscription
                        .getStatus()) {
                    subscriptionService.unsubscribe(subscription.getSource(),
                            subscription.getEvent(), user.getUsername(),
                            getSiteId());
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported subscription status '"
                                    + subscription.getStatus() + "'");
                }
            }
        }
        return true;
    }

}
