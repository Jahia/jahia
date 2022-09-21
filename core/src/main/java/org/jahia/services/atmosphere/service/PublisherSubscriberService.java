/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.atmosphere.service;

import org.apache.jackrabbit.util.Text;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.bin.Jahia;
import org.jahia.services.atmosphere.AtmosphereServlet;
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
     * Broadcast a message to a site.
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
     * Broadcast a message to a channel.
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
     * broadcast to a specific channel.
     * @param absoluteChannelName Name of the channel
     * @param message Message to broadcast
     */
    public void publishToAbsoluteChannel(String absoluteChannelName, String message) {
        broadcast(absoluteChannelName, message, false);
    }

    private void broadcast(String broadcasterID, String message, boolean createIfNull) {
        final BroadcasterFactory broadcasterFactory = AtmosphereServlet.getBroadcasterFactory();
        if (broadcasterFactory != null) {
            Broadcaster broadcaster = broadcasterFactory.lookup(broadcasterID, createIfNull);
            if (broadcaster != null) {
                broadcaster.broadcast(message);
            }
        }
    }


}
