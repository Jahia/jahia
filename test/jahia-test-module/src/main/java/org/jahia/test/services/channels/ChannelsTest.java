package org.jahia.test.services.channels;

import junit.framework.TestCase;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;

public class ChannelsTest extends TestCase {
    public void testGeneric() {
        assertTrue(ChannelService.getInstance().getAllChannels().contains("generic"));

        Channel channel = ChannelService.getInstance().getChannel("generic");

        assertEquals("root", channel.getFallBack());
        assertTrue(channel.isVisible());
    }
}
