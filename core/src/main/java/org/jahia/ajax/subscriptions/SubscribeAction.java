/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.subscriptions;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxDispatchAction;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.mail.MailServiceImpl;
import org.jahia.services.mail.MailSettings;
import org.jahia.services.notification.Subscription;
import org.jahia.services.notification.SubscriptionService.ConfirmationResult;
import org.jahia.services.notification.templates.TemplateUtils;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.mail.SimpleMailMessage;

/**
 * Ajax-based action handler for subscription management.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscribeAction extends AjaxDispatchAction {

    private static final Logger logger = Logger
            .getLogger(SubscribeAction.class);

    public ActionForward add(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        try {
            String pid = request.getParameter("pid");
            String key = getParameter(request, "key");
            String event = getParameter(request, "event");
            String askForConfirmation = request
                    .getParameter("confirmationRequired");
            ProcessingContext ctx = retrieveProcessingContext(request,
                    response, StringUtils.isNotEmpty(pid) ? "/op/edit/pid/"
                            + pid : null, true);

            if (Boolean.valueOf(askForConfirmation)) {
                Subscription subscription = ServicesRegistry.getInstance()
                        .getSubscriptionService()
                        .subscribeAndAskForConfirmation(key, true, event,
                                ctx.getUser().getUsername(), ctx.getSiteID());
                if (logger.isInfoEnabled()) {
                    logger
                            .info("Subscription is created and the confirmation message is sent to the subscriber."
                                    + " Subscription data: " + subscription);
                }
            } else {
                Subscription subscription = ServicesRegistry.getInstance()
                        .getSubscriptionService().subscribe(key, true, event,
                                ctx.getUser().getUsername(), ctx.getSiteID());
                if (logger.isInfoEnabled()) {
                    logger.info("Subscription created: " + subscription);
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward resultPage = null;

        try {
            String key = getParameter(request, "key");
            String[] data = null;
            int subscriptionId = 0;
            int siteId = 0;
            try {
                data = StringUtils.split(new String(Hex.decodeHex(key
                        .toCharArray())), "|");
                subscriptionId = Integer.parseInt(data[0]);
                siteId = Integer.parseInt(data[2]);
            } catch (Exception ex) {
                throw new JahiaBadRequestException(
                        "Wrong format for the 'key' parameter. Cause: "
                                + ex.getMessage(), ex);
            }

            ConfirmationResult confirmationResult = ServicesRegistry
                    .getInstance().getSubscriptionService().unsubscribe(
                            subscriptionId);

            request.setAttribute("confirmationResult", confirmationResult);
            resultPage = getPage(
                    "extensions/subscribable/unsubscribeConfirmation.jsp",
                    siteId);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return resultPage;
    }

    public ActionForward cancelWithConfirmation(ActionMapping mapping,
            ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        ActionForward resultPage = null;

        try {
            String key = getParameter(request, "key");
            String[] data = null;
            int subscriptionId = 0;
            String user = null;
            int siteId = 0;
            try {
                data = StringUtils.split(new String(Hex.decodeHex(key
                        .toCharArray())), "|");
                subscriptionId = Integer.parseInt(data[0]);
                user = data[1];
                siteId = Integer.parseInt(data[2]);
            } catch (Exception ex) {
                throw new JahiaBadRequestException(
                        "Wrong format for the 'key' parameter. Cause: "
                                + ex.getMessage(), ex);
            }

            Subscription subscription = ServicesRegistry.getInstance()
                    .getSubscriptionService().unsubscribeWithConfirmation(
                            subscriptionId, user, siteId);
            ConfirmationResult confirmationResult = subscription != null
                    && subscription.getConfirmationKey() != null ? ConfirmationResult.OK
                    : ConfirmationResult.SUBSCRIPTION_NOT_FOUND;

            request.setAttribute("confirmationResult", confirmationResult);
            resultPage = getPage(
                    "extensions/subscribable/unsubscribeRequest.jsp", siteId);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return resultPage;
    }

    public ActionForward confirm(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward resultPage = null;

        try {
            String key = getParameter(request, "key");
            String[] data = null;
            int subscriptionId = 0;
            String confirmationKey = null;
            int siteId = 0;
            try {
                data = StringUtils.split(new String(Hex.decodeHex(key
                        .toCharArray())), "|");
                subscriptionId = Integer.parseInt(data[0]);
                confirmationKey = data[1];
                siteId = Integer.parseInt(data[2]);
            } catch (Exception ex) {
                throw new JahiaBadRequestException(
                        "Wrong format for the 'key' parameter. Cause: "
                                + ex.getMessage(), ex);
            }

            ConfirmationResult confirmationResult = ServicesRegistry
                    .getInstance().getSubscriptionService()
                    .confirmSubscription(subscriptionId, confirmationKey);

            request.setAttribute("confirmationResult", confirmationResult);
            resultPage = getPage(
                    "extensions/subscribable/subscribeConfirmation.jsp", siteId);
        } catch (Exception e) {
            handleException(e, request, response);
        }

        return resultPage;
    }

    public ActionForward confirmCancel(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward resultPage = null;

        try {
            String key = getParameter(request, "key");
            String[] data = null;
            int subscriptionId = 0;
            String confirmationKey = null;
            int siteId = 0;
            try {
                data = StringUtils.split(new String(Hex.decodeHex(key
                        .toCharArray())), "|");
                subscriptionId = Integer.parseInt(data[0]);
                confirmationKey = data[1];
                siteId = Integer.parseInt(data[2]);
            } catch (Exception ex) {
                throw new JahiaBadRequestException(
                        "Wrong format for the 'key' parameter. Cause: "
                                + ex.getMessage(), ex);
            }

            ConfirmationResult confirmationResult = ServicesRegistry
                    .getInstance().getSubscriptionService().cancelSubscription(
                            subscriptionId, confirmationKey);

            request.setAttribute("confirmationResult", confirmationResult);
            resultPage = getPage(
                    "extensions/subscribable/unsubscribeConfirmation.jsp",
                    siteId);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return resultPage;
    }

    private ActionForward getPage(String page, int siteId) {
        String path = null;
        JahiaSite site = TemplateUtils.getSite(siteId);
        if (site != null) {
            path = new TemplatePathResolverBean(site.getTemplatePackageName())
                    .lookup(page);
        }

        return new ActionForward(path != null ? path : TemplateUtils
                .getTemplatesPath()
                + "default/" + page);
    }

    public ActionForward remove(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        try {
            String pid = getParameter(request, "pid");
            String key = getParameter(request, "key");
            String event = getParameter(request, "event");
            ProcessingContext ctx = retrieveProcessingContext(request,
                    response, "/op/edit/pid/" + pid, true);

            ServicesRegistry.getInstance().getSubscriptionService()
                    .unsubscribe(key, event, ctx.getUser().getUsername(),
                            ctx.getSiteID());

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    private void sendEmail(String host, String from, String to, String subject,
            String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);

        MailServiceImpl.getMailSender(
                new MailSettings(true, host, from, to, "Disabled")).send(msg);
    }

    public ActionForward testEmail(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        try {
            String host = getParameter(request, "host");
            String from = getParameter(request, "from");
            String to = getParameter(request, "to");

            JahiaUser user = (JahiaUser) request.getSession(true).getAttribute(
                    ProcessingContext.SESSION_USER);

            if (user == null || !user.isRoot()) {
                throw new JahiaUnauthorizedException(
                        "Action for sending test e-mail is available only for the super administator user.");
            }

            Locale locale = (Locale) request.getSession(true).getAttribute(
                    ProcessingContext.SESSION_LOCALE);
            locale = locale != null ? locale : request.getLocale();

            ResourceBundle resourceBundle = JahiaResourceBundle
                    .getAdminDefaultResourceBundle(null, locale);

            String subject = JahiaResourceBundle
                    .getString(
                            resourceBundle,
                            "org.jahia.admin.server.ManageServer.testSettings.mailSubject",
                            "Test message");
            String text = JahiaResourceBundle
                    .getString(
                            resourceBundle,
                            "org.jahia.admin.server.ManageServer.testSettings.mailText",
                            "Test message");

            sendEmail(host, from, to, subject, text);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            throw new JahiaBadRequestException(
                    "Required parameter 'action' is missing in the request.");
        } catch (Exception e) {
            handleException(e, request, response);
        }

        return null;
    }

}
