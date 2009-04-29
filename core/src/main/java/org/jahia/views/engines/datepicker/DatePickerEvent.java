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
package org.jahia.views.engines.datepicker;

import java.util.Date;
import java.util.EventObject;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;

public class DatePickerEvent extends EventObject
{

    private ProcessingContext   jParams;
    private Object      theObj;
    private long        eventTime;
    private JahiaData   jData; // this may be null if no JahiaData is
                                   // available.

    private DatePickerData datePickerData;

    /**
     * constructor
     * composes automatically the time when the event was triggered
     *
     * @param        source              the object that generated this event
     * @param        jParams             a ProcessingContext object (with request and response)
     * @param        jData               a JahiaData object, may be null if none is available
     * @param        object              the object targeted by this event.
     */
    public DatePickerEvent(  Object      source,
                             ProcessingContext   jParams,
                             JahiaData   jData,
                             Object      theObj,
                             DatePickerData datePickerData )
    {
        super( source );
        this.jParams    = jParams;
        this.eventTime  = (new Date()).getTime();
        this.theObj     = theObj;
        this.jData      = jData;
        this.datePickerData = datePickerData;
    }

    public ProcessingContext getParams() { return jParams; }
    public Object getObject() { return theObj; }
    public long getEventTime() { return eventTime; }
    public JahiaData getJahiaData() { return jData; }
    public void setEventTimeToNow(){
        this.eventTime = (new Date()).getTime();
    }

    public DatePickerData getDatePickerData(){
        return this.datePickerData;
    }
}