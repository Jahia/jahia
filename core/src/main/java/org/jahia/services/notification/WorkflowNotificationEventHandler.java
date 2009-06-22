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
                    new WorkflowMessageBuilder((JahiaUser) subscriber, events));
        } else {
            logger.warn("Unable to get current ProcessingContext instance."
                    + " Skip sending workflow notifications.");
        }
    }

}