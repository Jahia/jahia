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

import org.jahia.data.events.JahiaEvent;
import org.jahia.params.ProcessingContext;
import org.jahia.content.TimeBasedPublishingState;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 ao�t 2005
 * Time: 11:29:46
 * To change this template use File | Settings | File Templates.
 */
public class RetentionRuleEvent extends JahiaEvent implements TimeBasedPublishingState {

    public static final int DELETING_RULE = 1;
    public static final int ASSIGNING_NEW_RULE = 2;
    public static final int UPDATING_RULE = 3;
    public static final int RULE_SCHEDULING_NOTIFICATION = 4;

    public static final int VALID_FROM_DATE_REACHED = 0;
    public static final int VALID_TO_DATE_REACHED = 1;

    private int ruleId;
    private int eventType;
    private int dateReached;

    /**
     *
     * @param source
     * @param jParams
     * @param ruleId
     * @param eventType // DELETING_RULE, ASSIGNING_NEW_RULE, ...
     * @param dateReached // VALID_FROM_DATE_REACHED , VALID_TO_DATE_REACHED
     */
    public RetentionRuleEvent(Object source,
                              ProcessingContext jParams,
                              int ruleId,
                              int eventType,
                              int dateReached) {
        
        super(source, jParams, null);
        this.ruleId = ruleId;
        this.eventType = eventType;
        this.dateReached = dateReached;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getDateReached() {
        return dateReached;
    }

    public void setDateReached(int dateReached) {
        this.dateReached = dateReached;
    }
    
}
