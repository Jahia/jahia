/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.atmosphere.service;

import org.apache.jackrabbit.util.Text;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.utils.i18n.Messages;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Broadcast service
 *
 * used to publish (broadcast) String to receiver (site, channel, etc ..)
 *
 * User: rincevent
 * Date: 22/11/11
 * Time: 10:27 AM
 */
public class PublisherSubscriberService {

    private transient static Logger logger = LoggerFactory.getLogger(PublisherSubscriberService.class);

    /**
     * @param node Node of the site
     * @param message Message to broadcast
     */

    public void publishToSite(JCRNodeWrapper node, String message) {
        try {
            final JCRSiteNode resolveSite = node.getResolveSite();
            final List<Locale> activeLanguagesAsLocales = resolveSite.getActiveLiveLanguagesAsLocales();
            JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(node, "jnt:page");
            String pagePath = Text.escapePath(parentOfType != null ? parentOfType.getPath() : node.getPath()) + ".html";
            for (Locale activeLanguagesAsLocale : activeLanguagesAsLocales) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("body", Messages.get(resolveSite.getTemplatePackage(), message,
                            activeLanguagesAsLocale));
                } catch (MissingResourceException e) {
                    logger.warn(e.getMessage(), e);
                    jsonObject.put("body", message + " is missing or cannot be resolved");
                }
                // lookup for a parent of type page
                String path = "/cms/render";
                final String url =
                        Jahia.getContextPath() + path + "/" + resolveSite.getSession().getWorkspace().getName() + "/" +
                        activeLanguagesAsLocale + pagePath;
                jsonObject.put("url", url);
                jsonObject.put("name", node.getDisplayableName());
                broadcast(resolveSite.getSiteKey() + "-" + activeLanguagesAsLocale, jsonObject.toString(), false);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param node Node of the channel
     * @param message Message to broadcast
     */

    public void publishToNodeChannel(JCRNodeWrapper node, String message) {
        try {
            final JCRSiteNode resolveSite = node.getResolveSite();
            final List<Locale> activeLanguagesAsLocales = resolveSite.getActiveLiveLanguagesAsLocales();
            JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(node, "jnt:page");
            String pagePath = Text.escapePath(parentOfType != null ? parentOfType.getPath() : node.getPath()) + ".html";
            for (Locale activeLanguagesAsLocale : activeLanguagesAsLocales) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("body", Messages.get(resolveSite.getTemplatePackage(), message,
                            activeLanguagesAsLocale));
                } catch (MissingResourceException e) {
                    logger.warn(e.getMessage(), e);
                    jsonObject.put("body", message + " is missing or cannot be resolved");
                }
                String path = "/cms/render";
                final String url =
                        Jahia.getContextPath() + path + "/" + resolveSite.getSession().getWorkspace().getName() + "/" +
                        activeLanguagesAsLocale + pagePath;
                jsonObject.put("url", url);
                jsonObject.put("name", node.getDisplayableName());
                broadcast(node.getIdentifier() + "-" + activeLanguagesAsLocale, jsonObject.toString(), false);
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * broadcast to a specific channel
     * @param absoluteChannelName Name of the channel
     * @param message Message to broadcast
     */
    public void publishToAbsoluteChannel(String absoluteChannelName, String message) {
        broadcast(absoluteChannelName, message, false);
    }

    private void broadcast(String broadcasterID, String message, boolean createIfNull) {
        final BroadcasterFactory broadcasterFactory = BroadcasterFactory.getDefault();
        if (broadcasterFactory != null) {
            Broadcaster broadcaster = broadcasterFactory.lookup(broadcasterID, createIfNull);
            if (broadcaster != null) {
                broadcaster.broadcast(message);
            }
        }
    }


}
