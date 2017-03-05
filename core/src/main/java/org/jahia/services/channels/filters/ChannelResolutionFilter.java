/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.channels.filters;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

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

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACTIVE_CHANNEL_COOKIE_NAME.equals(cookie.getName())) {
                    // we quickly validate that the channel is allowed and recognized, since this is a value coming from the client.
                    Channel resolvedChannel = channelService.getChannel(cookie.getValue());
                    if (resolvedChannel != null) {
                        context.setChannel(resolvedChannel);
                        return null;
                    }
                }
            }
        }

        if (!StringUtils.isEmpty(context.getRequest().getParameter(ACTIVE_CHANNEL_QUERY_PARAMETER))
                && !Constants.LIVE_WORKSPACE.equals(context.getWorkspace())) {
            String activeChannel = context.getRequest().getParameter(ACTIVE_CHANNEL_QUERY_PARAMETER);
            Channel resolvedChannel = channelService.getChannel(activeChannel);
            if (resolvedChannel != null) {
                context.setChannel(resolvedChannel);
            } else {
                context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
            }
        }

        if (context.getChannel() == null) {
            Channel resolvedChannel = channelService.resolveChannel(context.getRequest());
            if (resolvedChannel != null) {
                context.setChannel(resolvedChannel);
                List l = (List) context.getRequest().getAttribute("module.cache.additional.key");
                if (l == null) {
                    l = new ArrayList();
                    context.getRequest().setAttribute("module.cache.additional.key", l);
                }
                l.add(resolvedChannel.getIdentifier());
            } else {
                context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
            }
        }
        return null;
    }
}