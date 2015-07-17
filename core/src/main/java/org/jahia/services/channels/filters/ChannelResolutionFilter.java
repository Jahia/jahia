/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
                && !Constants.LIVE_WORKSPACE.equals(context.getWorkspace().toString())) {
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