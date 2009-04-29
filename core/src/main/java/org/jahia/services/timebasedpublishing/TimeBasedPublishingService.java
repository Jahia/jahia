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

import org.jahia.content.ObjectKey;
import org.jahia.content.TimeBasedPublishingState;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.params.ProcessingContext;
import org.jahia.params.AdvPreviewSettings;

import java.util.Collection;
import java.util.List;
import java.util.Date;

public abstract class TimeBasedPublishingService extends JahiaService
        implements TimeBasedPublishingState {
    
    /**
     * Returns a RetentionRule instance
     *
     * @param id
     * @return
     */
    public abstract RetentionRule getRetentionRule(int id);

    /**
     * Returns a RetentionRule instance for the given ObjectKey
     *
     * @param objectKey
     * @return
     */
    public abstract RetentionRule getRetentionRule(ObjectKey objectKey);

    /**
     * Returns all RetentionRule instances
     *
     * @return
     */
    public abstract List getRetentionRules();

    /**
     * Saves and return the RetentionRule instance
     *
     * @param rule
     */
    public abstract void saveRetentionRule(RetentionRule rule) throws Exception;


    /**
     * Deletes the RetentionRule instance
     *
     * @param rule
     * @return
     */
    public abstract boolean deleteRetentionRule(RetentionRule rule) throws Exception;

    /**
     * Returns the base retention rule Def instance.
     * The one that must support at least inheritance
     * @return
     */
    public abstract RetentionRuleDef getBaseRetentionRuleDef();

    /**
     * Returns the RetentionRuleDef instance
     *
     * @param id
     * @return
     */
    public abstract RetentionRuleDef getRetentionRuleDef(Integer id);

    /**
     * Returns all RetentionRuleDef instances
     *
     * @return
     */
    public abstract List getRetentionRuleDefs();

    /**
     * Retrieve the object owning the retention rule and propagate retention event's to all it's child
     * if they inherit this retention rule
     *
     * @param theEvent
     */
    public abstract void handleTimeBasedPublishingEvent( RetentionRuleEvent theEvent )
    throws JahiaException;

    /**
     * return the list of inconsistent object in regards to the publishing state.
     *
     * @param checkTime the publication or expiration time
     * @param maxElapsedTime the elapsed time max above which the state should be considered inconsistent
     *
     * @return
     */
    public abstract List findInconsistentObjects(long checkTime, long maxElapsedTime);
    
    /**
     * Schedule a background job that will apply rule changes in background
     *
     * @param objectKey
     * @param operation
     * @param rule
     * @param jParams
     * @throws Exception
     */
    public abstract void scheduleBackgroundJob(ObjectKey objectKey, String operation, RetentionRule rule,
                                       ProcessingContext jParams) throws Exception;

    public abstract JahiaObjectManager getJahiaObjectMgr();
    
    /**
     * Returns child object keys of objects that implement TimeBasedPublishingJahiaObject interface
     * 
     * @param user
     * @param loadRequest
     * @param operationMode
     * @return
     */
    public abstract Collection getChildObjectKeysForTimeBasedPublishing(
            ObjectKey jahiaObjectKey, JahiaUser user,
            EntryLoadRequest loadRequest, String operationMode)
            throws JahiaException;    
    
    /**
     * Returns the parent object key of the current object for timebased publishing
     *
     * @return
     */
    public abstract ObjectKey getParentObjectKeyForTimeBasedPublishing(ObjectKey jahiaObjectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode)
            throws JahiaException;    
    
    /**
     * Returns the parent object key of the current object for timebased publishing
     */
    public abstract ObjectKey getParentObjectKeyForTimeBasedPublishing(ObjectKey jahiaObjectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode,
            boolean withoutInheritedRule) throws JahiaException;    


    /**
     * Returns true if the given object is valid checking agains the AdvPreviewSettings
     *
     * @param objectKey
     * @param user
     * @param loadRequest
     * @param operationMode
     * @param advPreviewSettings
     * @return
     * @throws JahiaException
     */
    public abstract boolean isValid(ObjectKey objectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode, AdvPreviewSettings advPreviewSettings)
    throws JahiaException;

    /**
     * Return true if the given object is valid at the given date
     * If the given date is null, returns delegate.isValid().
     *
     * @param objectKey
     * @param user
     * @param loadRequest
     * @param operationMode
     * @param date
     * @return
     * @throws JahiaException
     */
    public abstract boolean isValid(ObjectKey objectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode, Date date)
    throws JahiaException;

}
