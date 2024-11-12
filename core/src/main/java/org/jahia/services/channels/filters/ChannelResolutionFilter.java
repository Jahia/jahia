/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
