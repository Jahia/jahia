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
