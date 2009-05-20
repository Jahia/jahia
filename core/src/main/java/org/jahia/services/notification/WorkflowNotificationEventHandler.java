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
package org.jahia.services.notification;

import java.security.Principal;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.notification.templates.WorkflowMessageBuilder;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Handles workflow notification events.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowNotificationEventHandler extends
        EmailNotificationEventHandler {

    private static Logger logger = Logger
            .getLogger(WorkflowNotificationEventHandler.class);

    /**
     * Initializes an instance of this class.
     */
    public WorkflowNotificationEventHandler() {
        super();
        final boolean authorizedByLicense = LicenseActionChecker
                .isAuthorizedByLicense(
                        "org.jahia.actions.sites.*.engines.workflow.MailNotifications",
                        0);
        insertCondition(new Condition() {
            public boolean matches(Principal subscriber,
                    List<NotificationEvent> events) {
                if (!authorizedByLicense) {
                    logger
                            .info("Workflow mail notifications are not authorized by the current license."
                                    + " Skip sending notification.");
                }
                return authorizedByLicense;
            }

            public boolean matches(Subscription subscription,
                    List<NotificationEvent> events) {
                if (!authorizedByLicense) {
                    logger
                            .info("Workflow mail notifications are not authorized by the current license."
                                    + " Skip sending notification.");
                }
                return authorizedByLicense;
            }
        });
    }

    @Override
    protected void handleEvents(Principal subscriber,
            List<NotificationEvent> events) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling " + events.size() + " notification events: "
                    + events + " for subscriber: " + subscriber);
        } else {
            logger.info("Handling " + events.size()
                    + " notification event(s) for subscriber "
                    + subscriber.getName());
        }

        ProcessingContext ctx = Jahia.getThreadParamBean();
        if (ctx != null) {
            getMailService().sendTemplateMessage(
                    new WorkflowMessageBuilder((JahiaUser) subscriber, events,
                            ctx));
        } else {
            logger.warn("Unable to get current ProcessingContext instance."
                    + " Skip sending workflow notifications.");
        }
    }

}