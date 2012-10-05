package org.jahia.services.channels.filters;

import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import javax.jcr.Property;
import javax.jcr.Value;

/**
 * This filter will exclude a module rendering from a node if it has the associated configuration.
 */
public class ChannelExclusionFilter extends AbstractFilter {

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (resource.getNode().isNodeType("jmix:channelSelection")) {
            // we have a mixin applied, let's test if we must exclude it from the current channel.
            Property channelExclusionProperty = resource.getNode().getProperty("j:channelSelection");
            String includeOrExclude = resource.getNode().getProperty("j:channelIncludeOrExclude").getString();
            Value[] channelExclusionValues = channelExclusionProperty.getValues();
            Channel currentChannel = renderContext.getChannel();
            for (Value channelExclusionValue : channelExclusionValues) {
                if (channelExclusionValue.getString() != null) {
                    boolean inList = isInExclusionList(channelExclusionValue.getString(), currentChannel);
                    if (inList && includeOrExclude.equals("exclude")) {
                        return "";
                    }
                    if (inList && includeOrExclude.equals("include")) {
                        return null;
                    }
                }
            }
            if (includeOrExclude.equals("include")) {
                return "";
            }
        }
        return null;
    }

    private boolean isInExclusionList(String v, Channel channel) {
        if (v.equals(channel.getIdentifier())) {
            return true;
        } else {
            if (channel.getFallBack() != null && !channel.getFallBack().equals("root")) {
                return isInExclusionList(v, channelService.getChannel(channel.getFallBack()));
            }
        }
        return false;
    }

}
