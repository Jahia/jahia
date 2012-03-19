package org.jahia.services.channels;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A choicelist initializer for channel selection.
 */
public class ChannelChoiceListInitializer implements ChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ChannelChoiceListInitializer.class);

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> choiceListValues = new ArrayList<ChoiceListValue>();

        List<Channel> channels;
        channels = channelService.getAllChannels();
        for (Channel channel : channels) {
            choiceListValues.add(new ChoiceListValue(channel.getDisplayName(), channel.getIdentifier()));
        }
        return choiceListValues;
    }
}
