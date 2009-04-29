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

import java.io.Serializable;

/**
 * User subscription status.
 * 
 * @author Sergiy Shyrkov
 */
public enum SubscriptionStatus implements Serializable {
    /**
     * The user has not provided an e-mail address in the profile - no
     * subscription is available.
     */
    NO_EMAIL_ADDRESS,
    /**
     * The user is not subscribed to be notified for the events on the current
     * object - is allowed to subscribe.
     */
    NOT_SUBSCRIBED,
    /**
     * The user is currently subscribed to be notified for the events on the
     * current object - is allowed to unsubscribe.
     */
    SUBSCRIBED,
    /**
     * The user is not logged into the system - guest is not allowed to use
     * subscription service.
     */
    UNAUTHORIZED,
    /** The status is unknown or not requested yes. */
    UNKNOWN;
}