/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.channels;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * The channel service is the main service to access, retrieve, query, list and maybe even update channels
 */
public class ChannelService {

    List<ChannelProvider> channelProviders = new ArrayList<ChannelProvider>();

    private Map<String,Channel> channelMap = new HashMap<String,Channel>();

    private static volatile ChannelService instance = new ChannelService();

    public static ChannelService getInstance() {
        return instance;
    }

    public Channel getChannel(String identifier) {
        Channel result = channelMap.get(identifier);
        if (result != null) {
            return result;
        }
        result = new Channel();
        result.setIdentifier(identifier);
        for (ChannelProvider provider : channelProviders) {
            Map<String, String> channelCapabilities = provider.getChannelCapabilities(identifier);
            if (channelCapabilities != null) {
                result.getCapabilities().putAll(channelCapabilities);
            }
            if (provider.getAllChannels().contains(identifier)) {
                result.setFallBack(provider.getFallBack(identifier));
                result.setVisible(provider.isVisible(identifier));
            }
        }
        channelMap.put(identifier, result);
        return result;
    }

    public Channel resolveChannel(HttpServletRequest request) {
        String result = null;
        for (ChannelProvider provider : channelProviders) {
            result = provider.resolveChannel(request);
            if (result != null) {
                return getChannel(result);
            }
        }
        return null;
    }

    public List<String> getAllChannels() {
        List<String> result = new ArrayList<String>();
        for (ChannelProvider provider : channelProviders) {
            result.addAll(provider.getAllChannels());
        }
        return result;
    }

    public void addProvider(ChannelProvider provider) {
        if (channelProviders.contains(provider)) {
            channelProviders.remove(provider);
        }
        channelProviders.add(provider);
        Collections.sort(channelProviders, new Comparator<ChannelProvider>() {
            public int compare(ChannelProvider o1, ChannelProvider o2) {
                int i = o1.getPriority() - o2.getPriority();
                return i != 0 ? i : 1;
            }
        });
    }
}
