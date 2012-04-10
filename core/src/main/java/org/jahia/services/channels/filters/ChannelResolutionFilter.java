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

package org.jahia.services.channels.filters;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;

import javax.jcr.AccessDeniedException;
import javax.servlet.http.Cookie;

/**
 * A filter that will match a user agent and set it to the associated channel
 */
public class ChannelResolutionFilter extends AbstractFilter {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelResolutionFilter.class);
    public static final String ACTIVE_CHANNEL_COOKIE_NAME = "org.jahia.channels.activeChannel";
    public static final String ACTIVE_CHANNEL_QUERY_PARAMETER = "channel";

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        Cookie[] cookies = context.getRequest().getCookies();

        if (context.getChannel() != null) {
            return null;
        }

        if (!StringUtils.isEmpty(context.getRequest().getParameter(ACTIVE_CHANNEL_QUERY_PARAMETER))) {
            String activeChannel = context.getRequest().getParameter(ACTIVE_CHANNEL_QUERY_PARAMETER);
            Channel resolvedChannel = channelService.getChannel(activeChannel);
            if (resolvedChannel != null) {
                setChannel(context, resource, resolvedChannel);
                return null;
            }
        }

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACTIVE_CHANNEL_COOKIE_NAME.equals(cookie.getName())) {
                    // we quickly validate that the channel is allowed and recognized, since this is a value coming from the client.
                    Channel resolvedChannel = channelService.getChannel(cookie.getValue());
                    if (resolvedChannel != null) {
                        setChannel(context, resource, resolvedChannel);
                        return null;
                    }
                }
            }
        }

        if (!resource.getTemplateType().contains("-")) {
            Channel resolvedChannel = channelService.resolveChannel(context.getRequest());
            if (resolvedChannel != null) {
                setChannel(context, resource, resolvedChannel);
            }
        }
        return null;
    }

    private void setChannel(RenderContext context, Resource resource, Channel newChannel) throws AccessDeniedException {
        context.setChannel(newChannel);
        String resourceTemplate = resource.getTemplate();
        if (!resourceTemplate.contains("-")) {
            String resolvedTemplate = resource.getResolvedTemplate();
            // First we resolve the regular template
            Template originalTemplate = service.resolveTemplate(resource, context);
            // now let's try to see if a template exists for the current channel and resource
            if (originalTemplate != null) {
                Template lastTemplate = originalTemplate;
                while (lastTemplate.getNext() != null) {
                    lastTemplate = lastTemplate.getNext();
                }
                resource.setTemplate(lastTemplate.getName() + "-" + newChannel.getIdentifier());
                Template newChannelTemplate = service.resolveTemplate(resource, context);
                if (newChannelTemplate==null) {
                    if ("default".equals(resourceTemplate)) {
                        resource.setTemplate(null);
                    } else {
                        resource.setTemplate(resourceTemplate);
                    }
                }
            }
        }
        if (!resource.getTemplateType().contains("-")) {
            String baseType = resource.getTemplateType();
            resource.setTemplateType(baseType+"-"+newChannel.getIdentifier());
        }
    }

}