/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.timebasedpublishing;

import org.jahia.params.ProcessingContext;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 12:53:53
 * To change this template use File | Settings | File Templates.
 */
public interface RetentionRule extends Cloneable, Serializable  {
    
    public static final String RULE_NONE = "NONE";
    public static final String RULE_START_AND_END_DATE = "START_AND_END_DATE";
    public static final String RULE_DAILY = "DAILY";
    public static final String RULE_XDAYINWEEK = "XDAY_IN_WEEK";

    /**
     * The rule identifier
     */
    public Integer getId();

    public void setId(Integer id);

    /**
     * The rule title
     * @param title
     */
    public void setTitle(String title);

    public String getTitle();

    /**
     * The rule comment
     * @param title
     */
    public void setComment(String title);

    public String getComment();

    /**
     * If you, the rule can be shared
     * @param shared
     */
    public void setShared(Boolean shared);

    public Boolean getShared();

    /**
     * The rule definition
     */
    public RetentionRuleDef getRetentionRuleDef();

    public void setRetentionRuleDef(RetentionRuleDef retentionRuleDef);

    /**
     * Enabling or disabling the rule
     */
    public Boolean getEnabled();

    public void setEnabled(Boolean enabled);

    /**
     * The rule should inherit ( delegate to the object's parent's rule )
     */
    public Boolean getInherited();

    public void setInherited(Boolean inherited);

    /**
     * Returns the type of rule
     * @code RULE_START_AND_END_DATE, @code RULE_DAILY, @code RULE_XDAYINWEEK
     *
     * @return
     */
    public String getRuleType();

    public void setRuleType(String type);

    /**
     * A rule may start only at a given date
     * @return
     */
    public Long getDailyStartDate();

    /**
     * A rule may ends at a given date
     */
    public Long getDailyEndDate();

    /**
     * Returns an XML representation of all settings to be stored in database
     *
     */
    public String getSettings() throws Exception;

    /**
     * Save the rule state
     * @throws Exception
     */
    public void save() throws Exception;

    /**
     * Delete the rule
     * @throws Exception
     */
    public void delete() throws Exception;

    
    /**
     * Start the rule scheduling job
     * @return
     * @throws Exception
     */
    public boolean startJob() throws Exception;

    /**
     * Stop the rule scheduling job
     * @return
     * @throws Exception
     */
    public boolean stopJob() throws Exception;

    /**
     * Delete the rule scheduling job if any
     * @return
     * @throws Exception
     */
    public boolean deleteJob() throws Exception;
    
    /**
     * This method is called once a retention rule job finished.
     * Implementing classes should check for recurrence type and schedule next fire time
     *
     * @param context
     * @return the next fire time
     *
     * @throws Exception
     */
    public long scheduleNextJob(ProcessingContext context) throws Exception;

    /**
     * Return a list of ValidationErrors if any
     *
     * @param source
     * @return
     */
    public List validate(Object source);

    public Object clone() throws CloneNotSupportedException;

    /**
     * Returns true if the rule is valid at the given date
     *
     * @param date
     * @return
     * @throws Exception
     */
    public boolean isValid(Date date) throws Exception;
    
}
