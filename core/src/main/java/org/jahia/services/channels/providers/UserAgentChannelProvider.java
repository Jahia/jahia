package org.jahia.services.channels.providers;

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
public class UserAgentChannelProvider implements ChannelProvider, InitializingBean {

    public static final String USER_AGENT_HEADER_NAME = "user-agent";

    private int priority;

    private Map<String,Channel> channels = new HashMap<String,Channel>();
    private Map<Pattern,Channel> userAgentChannels = new HashMap<Pattern,Channel>();

    // the following list is used for Spring initialization, it is not used later on.
    List<Channel> channelList = new ArrayList<Channel>();

    private ChannelService channelService;

    public void setChannelList(List<Channel> channelList) {
        this.channelList = channelList;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public Map<String, String> getChannelCapabilities(String identifier) {
        if (channels.containsKey(identifier)) {
            return channels.get(identifier).getCapabilities();
        }
        return null;
    }

    public String resolveChannel(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER_NAME);
        if (userAgent != null) {
            for (Map.Entry<Pattern,Channel> entry : userAgentChannels.entrySet()) {
                Pattern curPattern = entry.getKey();
                Matcher m = curPattern.matcher(userAgent);
                if (m.matches()) {
                    return entry.getValue().getIdentifier();
                }
            }
        }
        return null;
    }

    public List<String> getAllChannels() {
        return Collections.unmodifiableList(new ArrayList<String>(channels.keySet()));
    }

}
