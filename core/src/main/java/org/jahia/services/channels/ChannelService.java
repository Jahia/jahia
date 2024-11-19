/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
            if (provider.getFallBack(identifier) != null) {
                result.setFallBack(provider.getFallBack(identifier));
            }
            if (provider.getAllChannels().contains(identifier)) {
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
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (ChannelProvider provider : channelProviders) {
            result.addAll(provider.getAllChannels());
        }
        return new ArrayList<String>(result);
    }

    public void addProvider(ChannelProvider provider) {
        if (channelProviders.contains(provider)) {
            channelProviders.remove(provider);
        }
        channelProviders.add(provider);
        channelMap.clear();
        Collections.sort(channelProviders, new Comparator<ChannelProvider>() {
            public int compare(ChannelProvider o1, ChannelProvider o2) {
                int i = o1.getPriority() - o2.getPriority();
                return i != 0 ? i : 1;
            }
        });
    }

    public boolean matchChannel(String channelId, Channel channel) {
        if (channel != null) {
            if (channelId.equals(channel.getIdentifier())) {
                return true;
            } else {
                if (channel.getFallBack() != null && !channel.getFallBack().equals("root")) {
                    return matchChannel(channelId, getChannel(channel.getFallBack()));
                }
            }
        }
        return false;
    }

    /**
     * Unregisters the specified provider.
     *
     * @param provider
     *            a provider to be removed
     */
    public void removeProvider(ChannelProvider provider) {
        channelProviders.remove(provider);
        channelMap.clear();
    }
}
