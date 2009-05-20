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
