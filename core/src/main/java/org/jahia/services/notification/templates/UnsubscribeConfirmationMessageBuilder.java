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

import org.jahia.bin.Jahia;
import org.jahia.services.notification.Subscription;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Creates Groovy-based e-mail messages for sending subscription confirmation
 * requests.
 * 
 * @author Sergiy Shyrkov
 */
public class UnsubscribeConfirmationMessageBuilder extends
        SubscriptionConfirmationMessageBuilder {

    /**
     * Initializes an instance of this class.
     * 
     * @param user
     * @param emailAddress
     * @param subscription
     *            subscriber information
     */
    public UnsubscribeConfirmationMessageBuilder(JahiaUser user,
            String emailAddress, Subscription subscription) {
        super(user, emailAddress, subscription);
    }

    protected Link getConfirmationLink() {
        Link lnk = null;
        String url = Jahia.getContextPath()
                + "/ajaxaction/subscription?action=confirmCancel&key="
                + getEncodedConfirmationKey();
        lnk = new Link("confirm", url, getServerUrl() + url);

        return lnk;
    }

    @Override
    protected String getTemplateHtmlPart() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/unsubscribeConfirmationBody.html");
    }

    @Override
    protected String getTemplateMailScript() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/unsubscribeConfirmation.groovy");
    }

    @Override
    protected String getTemplateTextPart() {
        // TODO consider event and node type also
        return lookupTemplate("notifications/subscription/unsubscribeConfirmationBody.txt");
    }

}