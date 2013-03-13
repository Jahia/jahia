/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.serversettings.flow.validator;

import org.apache.log4j.Logger;
import org.jahia.services.mail.MailService;
import org.jahia.services.mail.MailSettings;
import org.jahia.utils.i18n.Messages;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12/03/13
 */
public class MailSettingsValidator {
    private transient static Logger logger = Logger.getLogger(MailSettingsValidator.class);

    public void validateShowMailSettings(MailSettings mailSettings, ValidationContext validationContext) {
        logger.info("Validating mail settings");
        if (mailSettings.isServiceActivated()) {
            if (mailSettings.getFrom() == null || mailSettings.getFrom().isEmpty()) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source(
                        "from").defaultText(Messages.getInternal("message.mailFrom_mustSet.label",
                        LocaleContextHolder.getLocale())).build());
            }
            if (mailSettings.getNotificationSeverity() != 0 &&
                (mailSettings.getTo() == null || mailSettings.getTo().isEmpty())) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source("to").defaultText(
                        Messages.getInternal("message.mailAdmin_mustSet", LocaleContextHolder.getLocale())).build());
            }
            if (mailSettings.getUri() == null || mailSettings.getUri().isEmpty()) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source("uri").defaultText(
                        Messages.getInternal("message.mailServer_mustSet.label",
                                LocaleContextHolder.getLocale())).build());
            }
            if (mailSettings.getNotificationSeverity() != 0 && !MailService.isValidEmailAddress(mailSettings.getTo(),
                    true)) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source("to").defaultText(
                        Messages.getInternal("org.jahia.admin.JahiaDisplayMessage.enterValidEmailAdmin.label",
                                LocaleContextHolder.getLocale())).build());
            } else if (!MailService.isValidEmailAddress(mailSettings.getFrom(), false)) {
                validationContext.getMessageContext().addMessage(new MessageBuilder().error().source(
                        "from").defaultText(Messages.getInternal(
                        "org.jahia.admin.JahiaDisplayMessage.enterValidEmailFrom.label",
                        LocaleContextHolder.getLocale())).build());
            }
        } else {
            validationContext.getMessageContext().clearMessages();
        }
    }
}
