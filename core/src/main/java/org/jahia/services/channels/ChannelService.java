package org.jahia.services.channels;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * The channel service is the main service to access, retrieve, query, list and maybe even update channels
 */
public class ChannelService implements ChannelProvider {

    List<ChannelProvider> channelProviders = new ArrayList<ChannelProvider>();

    private static volatile ChannelService instance = new ChannelService();

    public static ChannelService getInstance() {
        return instance;
    }

    public Channel getChannel(String identifier) {
        Channel result = null;
        for (ChannelProvider provider : channelProviders) {
            result = provider.getChannel(identifier);
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    public Channel resolveChannel(HttpServletRequest request) {
        Channel result = null;
        for (ChannelProvider provider : channelProviders) {
            result = provider.resolveChannel(request);
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    public List<Channel> getAllChannels() {
        List<Channel> result = new ArrayList<Channel>();
        for (ChannelProvider provider : channelProviders) {
            result.addAll(provider.getAllChannels());
        }
        return result;
    }

    public void addProvider(ChannelProvider provider) {
        if (!channelProviders.contains(provider)) {
            channelProviders.add(provider);
        }
    }
}
