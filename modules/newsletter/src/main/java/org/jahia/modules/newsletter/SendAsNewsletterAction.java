/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.newsletter;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jahia.bin.ActionResult;
import org.jahia.bin.BaseAction;
import org.jahia.bin.Jahia;
import org.jahia.bin.Render;
import org.jahia.params.valves.TokenAuthValveImpl;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.HtmlExternalizationService;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.notification.Subscription;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.render.*;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.PaginatedList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.*;

/**
 * An action and a background task that sends the content of the specified node as a newsletter
 * to its subscribers.
 * 
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public class SendAsNewsletterAction extends BaseAction implements BackgroundAction {

	private static final String J_LAST_SENT = "j:lastSent";

	private static final String J_SCHEDULED = "j:scheduled";

	private static final Logger logger = LoggerFactory.getLogger(SendAsNewsletterAction.class);

    private HtmlExternalizationService htmlExternalizationService;
    private HttpClientService httpClientService;
    private MailService mailService;
    private RenderService renderService;
    private SubscriptionService subscriptionService;

    public ActionResult doExecute(HttpServletRequest req, final RenderContext renderContext,
            Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver)
            throws Exception {
        final JCRNodeWrapper node = resource.getNode();

        logger.info("Sending content of the node {} as a newsletter", node);

        long timer = System.currentTimeMillis();

        try {

            final Map<String, String> newsletterVersions = new HashMap<String, String>();

            if (req.getParameter("testemail") != null) {
                sendNewsletter(renderContext, node, req.getParameter("testemail"), req.getParameter("user"), req.getParameter("type"),
                        LanguageCodeConverters.languageCodeToLocale(req.getParameter("locale")), "live",
                        newsletterVersions);
            } else {
                final boolean personalized = node.hasProperty("j:personalized") && node.getProperty("j:personalized").getBoolean();

                JCRTemplate.getInstance().doExecuteWithSystemSession(null,"live", new JCRCallback() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        PaginatedList<Subscription> l = subscriptionService.getSubscriptions(node.getParent().getIdentifier(), null,false,0,0,
                                session);
                        for (Subscription subscription : l.getData()) {
                            String username = "guest";
                            if (personalized && subscription.getSubscriber() != null) {
                                username = subscription.getSubscriber();
                            }
                            if (subscription.getEmail() != null && subscription.isConfirmed() && !subscription.isSuspended()) {
                                sendNewsletter(renderContext, node, subscription.getEmail(), username, "html",
                                        LanguageCodeConverters.languageCodeToLocale(node.getResolveSite().getDefaultLanguage()), "live",
                                        newsletterVersions);
                            }
                        }
                        return null;
                    }
                });

                node.checkout();
                node.setProperty(J_SCHEDULED, (Value) null);
                node.setProperty(J_LAST_SENT, Calendar.getInstance());
                node.getSession().save();
            }
        } catch (RepositoryException e) {
            logger.warn("Unable to update properties for node " + node.getPath(), e);
        }

        logger.info("The content of the node {} was sent as a newsletter in {} ms", node.getPath(),
                System.currentTimeMillis() - timer);

	    return ActionResult.OK;
    }

    private void sendNewsletter(final RenderContext renderContext, final JCRNodeWrapper node, final String email,
                                final String user, final String type, final Locale locale, final String workspace, final Map<String, String> newsletterVersions)
            throws RepositoryException {

        final String id = node.getIdentifier();

        final String key = locale + user + type;
        if (!newsletterVersions.containsKey(key)) {
            JCRTemplate.getInstance().doExecute(false, user, workspace, locale, new JCRCallback() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper node = session.getNodeByIdentifier(id);
                        String out = renderService.render(new Resource(node, "html", null, "page"), renderContext);
                        out = htmlExternalizationService.externalize(out, renderContext);

                        newsletterVersions.put(key, out);

                        String title = node.getName();
                        if (node.hasProperty("jcr:title")) {
                            title = node.getProperty("jcr:title").getString();
                        }
                        newsletterVersions.put(key + ".title", title);
                    } catch (RenderException e) {
                        throw new RepositoryException(e);
                    }
                    return null;
                }
            });
        }
        String out = newsletterVersions.get(key);
        String subject = newsletterVersions.get(key + ".title");
        mailService.sendHtmlMessage(mailService.defaultSender(), email, null,null,subject, out);
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        // do local post on node.getPath/sendAsNewsletter.do
    	try {
            Map<String,String> headers = new HashMap<String,String>();
            headers.put("jahiatoken",TokenAuthValveImpl.addToken(node.getSession().getUser()));
			String out = httpClientService.executePost("http://localhost:8080"+
			        Jahia.getContextPath() + Render.getRenderServletPath() + "/live/"
			                + node.getResolveSite().getDefaultLanguage() + node.getPath()
			                + ".sendAsNewsletter.do", null, headers);
			logger.info(out);
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
    }

    public void setHtmlExternalizationService(HtmlExternalizationService htmlExternalizationService) {
        this.htmlExternalizationService = htmlExternalizationService;
    }

    public void setHttpClientService(HttpClientService httpClientService) {
    	this.httpClientService = httpClientService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

	public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

}
