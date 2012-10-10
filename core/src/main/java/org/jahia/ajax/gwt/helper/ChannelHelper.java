package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.bin.Jahia;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;

import java.util.ArrayList;
import java.util.List;

/**
 * Service helper to access channel data.
 */
public class ChannelHelper {

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public List<GWTJahiaChannel> getChannels() {
        List<String> channels = channelService.getAllChannels();
        List<GWTJahiaChannel> gwtJahiaChannels = new ArrayList<GWTJahiaChannel>();
        for (String channelName : channels) {
            Channel channel = channelService.getChannel(channelName);
            if (channel.isVisible()) {
                String imageURL = channel.getCapability("device-image");
                if (imageURL == null) {
                    imageURL = "/engines/images/edit/devices/default-small.png";
                }
                GWTJahiaChannel gwtJahiaChannel = new GWTJahiaChannel(channel.getIdentifier(), channel.getCapability("display-name"), Jahia.getContextPath()+imageURL, channel.getCapabilities());
                gwtJahiaChannels.add(gwtJahiaChannel);
            }
        }
        return gwtJahiaChannels;
    }
}
