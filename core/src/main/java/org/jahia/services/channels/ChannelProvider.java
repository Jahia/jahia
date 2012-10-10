package org.jahia.services.channels;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * A channel provider will implement the functionality to resolve and provide list of channels
 */
public interface ChannelProvider {

    public int getPriority();

    public Map<String,String> getChannelCapabilities(String identifier);

    public String resolveChannel(HttpServletRequest request);

    public List<String> getAllChannels();

    public String getFallBack(String identifier);

    public boolean isVisible(String identifier);

}
