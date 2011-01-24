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

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Jahia;
import org.jahia.bin.Render;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.TokenAuthValveImpl;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.HtmlExternalizationService;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.notification.Subscription;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.render.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.PaginatedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * An action and a background task that sends the content of the specified node as a newsletter
 * to its subscribers.
 * 
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public class SendAsNewsletterAction extends Action implements BackgroundAction {

	private static final int READ_CHUNK_SIZE = 1000;

	private static final String J_LAST_SENT = "j:lastSent";

	private static final String J_SCHEDULED = "j:scheduled";

	private static final Logger logger = LoggerFactory.getLogger(SendAsNewsletterAction.class);

    private HtmlExternalizationService htmlExternalizationService;
    private HttpClientService httpClientService;
    private MailService mailService;
    private RenderService renderService;
    private JahiaSitesService siteService;
    private SubscriptionService subscriptionService;
    private JahiaUserManagerService userService;

    public ActionResult doExecute(final HttpServletRequest req, final RenderContext renderContext,
                                  Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver)
            throws Exception {
        final JCRNodeWrapper node = resource.getNode();

        logger.info("Sending content of the node {} as a newsletter", node.getPath());

        long timer = System.currentTimeMillis();

        try {

            final Map<String, String> newsletterVersions = new HashMap<String, String>();

            if (req.getParameter("testemail") != null) {
                sendNewsletter(renderContext, node, req.getParameter("testemail"), req.getParameter("user"), req.getParameter("type"),
                        LanguageCodeConverters.languageCodeToLocale(req.getParameter("locale")), "live",
                        newsletterVersions);
            } else {
                final boolean personalized = node.hasProperty("j:personalized") && node.getProperty("j:personalized").getBoolean();

                JCRTemplate.getInstance().doExecuteWithSystemSession(null,"live", new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    	boolean saveSession = false;
                        PaginatedList<Subscription> l = null;
                        int total = 0;
                        int offset = 0;
                        JCRNodeWrapper target = node.getParent();
                        do {
                            l = subscriptionService.getSubscriptions(target.getIdentifier(), true, true, null, false, offset, READ_CHUNK_SIZE, session);
                            total = l.getTotalSize();
                            for (Subscription subscription : l.getData()) {
                                if (StringUtils.isEmpty(subscription.getEmail())) {
                                	logger.warn("Empty e-mail found for the subscription {}. Skipping.", subscription.getSubscriber());
                                	continue;
                                }
                            	
                                String username = "guest";

                                JahiaSite site = null;
                                try {
                                    site = siteService.getSiteByKey(node.getResolveSite().getSiteKey());
                                } catch (JahiaException e) {
                                }

                                if (personalized && subscription.isRegisteredUser() && subscription.getSubscriber() != null) {
                                    username = subscription.getSubscriber();
                                }

                                JahiaUser user = subscription.isRegisteredUser() ? userService.lookupUserByKey(subscription.getSubscriber()) : userService.lookupUser("guest");
                                RenderContext letterContext = new RenderContext(renderContext.getRequest(), renderContext.getResponse(), user);
                                Locale language = subscription.isRegisteredUser() ? 
                                		UserPreferencesHelper.getPreferredLocale(user , site) : 
                                			LanguageCodeConverters.languageCodeToLocale(subscription.getProperties().get("j:preferredLanguage"));
                                if (language == null) {
                                	language = LanguageCodeConverters.languageCodeToLocale(site != null ? site.getDefaultLanguage() : SettingsBean.getInstance().getDefaultLanguageCode());
                                }
                                String confirmationKey = subscription.getConfirmationKey();
                                if (confirmationKey == null) {
                                	try {
	                                	JCRNodeWrapper subscriptionNode = session.getNodeByUUID(subscription.getId());
	                                	confirmationKey = subscriptionService.generateConfirmationKey(subscriptionNode);
	                                	letterContext.getRequest().setAttribute("org.jahia.modules.newsletter.unsubscribeLink", UnsubscribeAction.generateUnsubscribeLink(target, confirmationKey, req));
	                                	subscriptionNode.setProperty(SubscriptionService.J_CONFIRMATION_KEY, confirmationKey);
	                                	saveSession = true;
                                	} catch (RepositoryException e) {
										        logger.warn(
										                "Unable to store the confirmation key for the subscription "
										                        + subscription.getSubscriber(), e);
                                	}
                                } else {
                                	letterContext.getRequest().setAttribute("org.jahia.modules.newsletter.unsubscribeLink", UnsubscribeAction.generateUnsubscribeLink(target, confirmationKey, req));
                                }
                                sendNewsletter(letterContext, node, subscription.getEmail(), username, "html",
                                            language, "live",
                                            newsletterVersions);
                            }
                            
                            offset += READ_CHUNK_SIZE;
                        } while (offset < total);
                        
                        if (saveSession) {
                        	session.save();
                        }
                        
                        return Boolean.TRUE;
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
            JCRTemplate.getInstance().doExecute(false, user, workspace, locale, new JCRCallback<String>() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper node = session.getNodeByIdentifier(id);
                        Resource resource = new Resource(node, "html", null, "page");
                        renderContext.setMainResource(resource);
                        renderContext.setSite(node.getResolveSite());

                        // Clear attributes
                        @SuppressWarnings("rawtypes")
                        Enumeration attributeNames = renderContext.getRequest().getAttributeNames();
                        while (attributeNames.hasMoreElements()) {
                        	String attr = (String) attributeNames.nextElement();
                        	if (!attr.startsWith("org.jahia.modules.newsletter.")) {
                        		renderContext.getRequest().removeAttribute(attr);
                        	}
                        }

                        String out = renderService.render(resource, renderContext);
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
        if (logger.isDebugEnabled()) {
            logger.debug("Send newsltter to "+email + " , subject " + subject);
            logger.debug(out);
        }
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

	public void setUserService(JahiaUserManagerService userService) {
    	this.userService = userService;
    }

	public void setSiteService(JahiaSitesService siteService) {
    	this.siteService = siteService;
    }

}
