package org.jahia.modules.serversettings.flow.validator;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.jahia.services.mail.MailService;
import org.jahia.services.mail.MailSettings;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author rincevent
 */
public class MailSettingsValidator implements Serializable {
    private static final long serialVersionUID = -8286051616441316996L;
    private transient static Logger logger = Logger.getLogger(MailSettingsValidator.class);

    public void validateShowMailSettings(MailSettings mailSettings, ValidationContext validationContext) {
        logger.info("Validating mail settings");
        if (mailSettings.isServiceActivated()) {
            if (mailSettings.getFrom() == null || mailSettings.getFrom().isEmpty()) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source(
                        "from").defaultText(Messages.get("resources.JahiaServerSettings", "serverSettings.mailServerSettings.errors.from.mandatory",
                        LocaleContextHolder.getLocale())).build());
            } else if (!MailService.isValidEmailAddress(mailSettings.getFrom(), false)) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source(
                        "from").defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                        "serverSettings.mailServerSettings.errors.email",
                        LocaleContextHolder.getLocale(),"from")).build());
            }
            if (mailSettings.getNotificationSeverity() != 0 &&
                (mailSettings.getTo() == null || mailSettings.getTo().isEmpty())) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source("to").defaultText(
                        Messages.getInternal("serverSettings.mailServerSettings.errors.administrator.mandatory", LocaleContextHolder.getLocale())).build());
            } else if (mailSettings.getNotificationSeverity() != 0 && !MailService.isValidEmailAddress(
                    mailSettings.getTo(), true)) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source(
                        "to").defaultText(Messages.getWithArgs("resources.JahiaServerSettings",
                        "serverSettings.mailServerSettings.errors.email",
                        LocaleContextHolder.getLocale(),"administrator")).build());
            }
            if (mailSettings.getUri() == null || mailSettings.getUri().isEmpty()) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source("uri").defaultText(
                        Messages.get("resources.JahiaServerSettings", "serverSettings.mailServerSettings.errors.server.mandatory",
                                LocaleContextHolder.getLocale())).build());
            }

        } else {
            validationContext.getMessageContext().clearMessages();
        }
    }
}
