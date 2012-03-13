package org.jahia.services.channels;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A channel provider will implement the functionality to resolve and provide list of channels
 */
public interface ChannelProvider {

    public Channel getChannel(String identifier);

    public Channel resolveChannel(HttpServletRequest request);

    public List<Channel> getAllChannels();

}
