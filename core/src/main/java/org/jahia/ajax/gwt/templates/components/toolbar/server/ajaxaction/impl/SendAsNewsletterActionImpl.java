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
package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.events.JahiaEventGeneratorService;
import org.jahia.services.notification.NotificationEvent;
import org.jahia.services.notification.NotificationService;
import org.jahia.utils.i18n.JahiaResourceBundle;

/**
 * Ajax action handler for sending the current page as a newsletter.
 * 
 * @author Sergiy Shyrkov
 */
public class SendAsNewsletterActionImpl extends AjaxAction {

    private static final transient Logger logger = Logger
            .getLogger(SendAsNewsletterActionImpl.class);

    private JahiaResourceBundle bundle;

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction
     * #execute(org.jahia.data.JahiaData, java.lang.String, java.util.Map)
     */
    @Override
    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action,
            Map<String, GWTJahiaProperty> gwtPropertiesMap) {
        String result = "OK";
        ProcessingContext ctx = jahiaData.getProcessingContext();
        bundle = new JahiaResourceBundle(null, ctx.getLocale(), ctx.getSite()
                .getTemplatePackageName());

        if (!ctx.getPage().hasActiveEntries()) {
            result = bundle
                    .get(
                            "toolbar.subscriptions.button.newsletter.result.noLiveVersion",
                            "Current page was never published before."
                                    + " Only the live version of a page can be sent as a newsletter.");
        } else {
            try {
                if (action != null && "test".equals(action)) {
                    result = testNewsletter(gwtPropertiesMap.get("comment")
                            .getValue(), ctx);
                } else {
                    result = fireEvent(ctx);
                }
            } catch (Exception e) {
                logger.error("Error sending newsletter. Cause: "
                        + e.getMessage(), e);
                result = bundle
                        .get(
                                "toolbar.subscriptions.button.newsletter.result.failure",
                                "Sending of the newsletter failed. Cause:")
                        + " " + e.getMessage();
            }
        }
        return new GWTJahiaAjaxActionResult(result);
    }

    private String fireEvent(ProcessingContext ctx) {
        NotificationEvent evt = new NotificationEvent(ctx.getPage()
                .getContentPage().getObjectKey().getKey(), "newsletter");
        evt.setSiteId(ctx.getSiteID());
        evt.setPageId(ctx.getPageID());
        JahiaEventGeneratorService evtService = ServicesRegistry.getInstance().getJahiaEventService();
        evtService.fireNotification(evt);
        evtService.fireAggregatedEvents();
        return bundle.get("toolbar.subscriptions.button.newsletter.result.ok",
                "The process of newsletter sending started successfully.");
    }

    private String testNewsletter(final String recipient,
            final ProcessingContext ctx) {
        final String content = NotificationService.getInstance()
                .getPageAsNewsletter(ctx, ctx.getUser(),
                        ctx.getLocale().toString(), ctx.getPageID());

        ServicesRegistry.getInstance().getMailService().sendHtmlMessage(
                null,
                recipient,
                null,
                null,
                ctx.getPage().getContentPage().getTitle(
                        ctx.getEntryLoadRequest(), false), content);

        return bundle.get("toolbar.subscriptions.button.newsletter.result.ok",
                "The process of newsletter sending started successfully.");
    }

}
