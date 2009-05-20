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
//  EV      12.01.2001
//  SH      12.12.2001 Adding JahiaData reference, only for reasons of speed
//  because in some cases we were recreating it instead of using the already
//  existing one.
//

package org.jahia.data.events;

import java.util.EventObject;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;

public class JahiaEvent extends EventObject implements JahiaEventInterface {

    private ProcessingContext jParams;
    private Object theObj;
    private long eventTime;
    private JahiaData jData; // this may be null if no JahiaData is
    // available.

    /***
     * constructor
     * composes automatically the time when the event was triggered
     *
     * @param        source              the object that generated this event
     * @param        jParams             a ProcessingContext object (with request and response)
     * @param        jData               a JahiaData object, may be null if none is available
     * @param        objectID            the object id targeted by this event (i.e. the field id)
     *
     */
    public JahiaEvent (Object source,
                       ProcessingContext jParams,
                       JahiaData jData,
                       Object theObj) {
        super(source);
        this.jParams = jParams;
        this.eventTime = System.currentTimeMillis();
        this.theObj = theObj;
        this.jData = jData;
    } // end JahiaEvent

    /***
     * constructor
     * composes automatically the time when the event was triggered
     *
     * @param        source              the object that generated this event
     * @param        jParams             a ProcessingContext object (with request and response)
     * @param        objectID            the object id targeted by this event (i.e. the field id)
     *
     */
    public JahiaEvent (Object source,
                       ProcessingContext jParams,
                       Object theObj) {
        this(source, jParams, null, theObj);
    } // end JahiaEvent

    /**
     * @return
     * @deprecated use getProcessingContext instead
     */
    public ProcessingContext getParams () {
        return jParams;
    }

    public ProcessingContext getProcessingContext () {
        return jParams;
    }

    public Object getObject () {
        return theObj;
    }

    public long getEventTime () {
        return eventTime;
    }

    public JahiaData getJahiaData () {
        return jData;
    }

    public String toString() {
        return getClass().getName() + "[source=" + source + ", Obj="+theObj+"]";
    }

} // end JahiaEvent
