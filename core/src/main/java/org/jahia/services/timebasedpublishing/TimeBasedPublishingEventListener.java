/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.timebasedpublishing;

import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaEventListener;

/**
 *
 */
public class TimeBasedPublishingEventListener extends JahiaEventListener {

    private static Logger logger = Logger.getLogger(TimeBasedPublishingEventListener.class);

    private TimeBasedPublishingService tbpServ = null;

    public TimeBasedPublishingService getTbpServ() {
        return tbpServ;
    }

    public void setTbpServ(TimeBasedPublishingService tbpServ) {
        this.tbpServ = tbpServ;
    }

    public void timeBasedPublishingEvent( RetentionRuleEvent theEvent )
    {
        try {
            tbpServ.handleTimeBasedPublishingEvent(theEvent);
        } catch ( Exception t){
            logger.debug("Exception occurent on event :timeBasedPublishingEvent",t);
        }
    }
}
