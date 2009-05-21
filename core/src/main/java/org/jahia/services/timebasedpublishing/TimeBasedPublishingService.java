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
