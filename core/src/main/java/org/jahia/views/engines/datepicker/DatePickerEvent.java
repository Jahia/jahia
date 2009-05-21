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