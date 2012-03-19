package org.jahia.services.channels.filters;

import org.jahia.services.channels.Channel;
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

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (resource.getNode().isNodeType("jmix:channelExclusion")) {
            // we have a mixin applied, let's test if we must exclude it from the current channel.
            Property channelExclusionProperty = resource.getNode().getProperty("j:channelExclusions");
            Value[] channelExclusionValues = channelExclusionProperty.getValues();
            Channel currentChannel = renderContext.getChannel();
            for (Value channelExclusionValue : channelExclusionValues) {
                if (channelExclusionValue.getString() != null &&
                        channelExclusionValue.getString().equals(currentChannel.getIdentifier())) {
                    return "";
                }
            }
        }
        return null;
    }

}
