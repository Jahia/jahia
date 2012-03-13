package org.jahia.services.channels.providers.basic;

import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelProvider;
import org.jahia.services.channels.ChannelService;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic channel provider configured through Spring files
 */
public class BasicChannelProvider implements ChannelProvider, InitializingBean {

    public static final String USER_AGENT_HEADER_NAME = "user-agent";

    Map<String,Channel> channels = new HashMap<String,Channel>();
    Map<Pattern,Channel> userAgentChannels = new HashMap<Pattern,Channel>();

    // the following list is used for Spring initialization, it is not used later on.
    List<Channel> channelList = new ArrayList<Channel>();

    private ChannelService channelService;

    public void setChannelList(List<Channel> channelList) {
        this.channelList = channelList;
        if (!channelList.contains(Channel.DEFAULT_CHANNEL)) {
            channelList.add(0, Channel.DEFAULT_CHANNEL);
        }
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void afterPropertiesSet() throws Exception {
        for (Channel channel : channelList) {
            channels.put(channel.getIdentifier(), channel);
            if (channel.hasCapabilityValue("userAgentPattern")) {
                Pattern curPattern = Pattern.compile(channel.getCapability("userAgentPattern"));
                userAgentChannels.put(curPattern, channel);
            }
        }
        channelService.addProvider(this);
    }

    public Channel getChannel(String identifier) {
        return channels.get(identifier);
    }

    public Channel resolveChannel(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER_NAME);
        if (userAgent != null) {
        for (Map.Entry<Pattern,Channel> entry : userAgentChannels.entrySet()) {
            Pattern curPattern = entry.getKey();
            Matcher m = curPattern.matcher(userAgent);
            if (m.matches()) {
                return entry.getValue();
            }
        }
        }
        return null;
    }

    public List<Channel> getAllChannels() {
        return Collections.unmodifiableList(new ArrayList<Channel>(channelList));
    }

}
