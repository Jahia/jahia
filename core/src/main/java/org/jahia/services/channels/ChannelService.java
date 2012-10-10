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
