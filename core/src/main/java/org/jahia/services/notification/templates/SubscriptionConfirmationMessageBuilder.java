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
package org.jahia.services.notification.templates;

import groovy.lang.Binding;

import org.apache.commons.codec.binary.Hex;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObjectKey;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.notification.Subscription;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Creates Groovy-based e-mail messages for sending subscription confirmation
 * requests.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionConfirmationMessageBuilder extends MessageBuilder {

    private String objectType;

    protected Subscription subscription;

    /**
     * Initializes an instance of this class.
     * 
     * @param user
     * @param emailAddress
     * @param subscription
     *            subscriber information
     */
    public SubscriptionConfirmationMessageBuilder(JahiaUser user,
            String emailAddress, Subscription subscription) {
        super(user, emailAddress, subscription.getSiteId());
        this.subscription = subscription;
    }

    protected Link getCancellationLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=confirmCancel&key="
                + getEncodedConfirmationKey();
        lnk = new Link("confirmCancel", url, getServerUrl() + url);

        return lnk;
    }

    protected Link getConfirmationLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=confirm&key="
                + getEncodedConfirmationKey();
        lnk = new Link("confirm", url, getServerUrl() + url);

        return lnk;
    }

    protected String getEncodedConfirmationKey() {
        return new String(Hex.encodeHex((subscription.getId() + "|"
                + subscription.getConfirmationKey() + "|" + subscription
                .getSiteId()).getBytes()));
    }

    protected String getObjectType() {
        if (objectType == null) {
            ContentObjectKey contentObjectKey = null;
            try {
                contentObjectKey = (ContentObjectKey) ContentObjectKey
                        .getInstance(subscription.getObjectKey());
            } catch (ClassNotFoundException e) {
                // not a content object key
            }
            objectType = JCRContentUtils
                    .cleanUpNodeName(contentObjectKey != null ? JCRContentUtils
                            .getNodeTypeName(contentObjectKey) : subscription
                            .getObjectKey());
        }

        return objectType;
    }

    @Override
    protected String getTemplateHtmlPart() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/subscribeConfirmationBody.html");
    }

    @Override
    protected String getTemplateMailScript() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/subscribeConfirmation.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/subscribeConfirmationBody.txt");
    }

    protected Link getUnsubscribeLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=cancel&key="
                + new String(Hex.encodeHex((subscription.getId() + "|"
                        + subscription.getUsername() + "|" + subscription
                        .getSiteId()).getBytes()));
        lnk = new Link("confirm", url, getServerUrl() + url);

        return lnk;
    }

    protected Link getWatchedContentLink() {
        return getWatchedContentLink(subscription.getObjectKey());
    }

    @Override
    protected void populateBinding(Binding binding) {
        super.populateBinding(binding);
        binding.setVariable("eventType", subscription.getEventType());
        binding.setVariable("subscription", subscription);
        binding.setVariable("confirmationLink", getConfirmationLink());
        binding.setVariable("cancellationLink", getCancellationLink());
        binding.setVariable("watchedContentLink", getWatchedContentLink());
    }
}