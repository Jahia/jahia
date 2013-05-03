/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.mail;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ExternalEventListener;
import org.jahia.services.mail.MailServiceImpl.MailSettingsChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Listener for external (from other cluster nodes) change events for the mail server settings.
 * 
 * @author Sergiy Shyrkov
 */
public class MailSettingsListener extends DefaultEventListener implements ExternalEventListener,
        ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(MailSettingsListener.class);

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public int getEventTypes() {
        return Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    @Override    
    public String[] getNodeTypes() {
        return new String[] { "jnt:mailServerSettings" };
    }
    
    @Override
    public String getPath() {
        return "/settings/mail-server";
    }

    public void onEvent(EventIterator events) {
        boolean external = false;
        while (events.hasNext()) {
            if (isExternal(events.nextEvent())) {
                external = true;
                break;
            }
        }
        if (!external) {
            return;
        }

        logger.info("Event received about changes in mail server connection settings."
                + " Notifying mail service...");
        applicationEventPublisher.publishEvent(new MailSettingsChangedEvent(this));
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
