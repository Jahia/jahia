package org.jahia.modules.serversettings.flow.validator;

import org.apache.log4j.Logger;
import org.jahia.services.search.SearchServiceImpl;
import org.jahia.services.search.SearchSettings;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;

import java.io.Serializable;

/**
 * Created by kevan on 22/07/14.
 */
public class SearchSettingsValidator implements Serializable {
    private static final long serialVersionUID = 828187966326333450L;
    private transient static Logger logger = Logger.getLogger(SearchSettingsValidator.class);

    public void validateShowSearchSettings(SearchSettings searchSettings, ValidationContext validationContext) {
        logger.info("Validating search settings");
        if(searchSettings.getCurrentProvider() == null || searchSettings.getCurrentProvider().isEmpty()
                || !SearchServiceImpl.getInstance().getAvailableProviders().contains(searchSettings.getCurrentProvider())) {
            validationContext.getMessageContext().addMessage(
                    new MessageBuilder().error().source("currentProvider")
                            .code("serverSettings.searchServerSettings.errors.provider.invalid").build());
        } else {
            validationContext.getMessageContext().clearMessages();
        }
    }
}
