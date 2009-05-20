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
package org.jahia.taglibs.internal.date;

import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.engines.calendar.CalendarHandler;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public abstract class AbstractDateTag extends ValueJahiaTag {
    protected static final String DATE_PATTERN_NO_TIME = CalendarHandler.DEFAULT_DATEONLY_FORMAT;
    protected static final String DATE_PATTERN_TIME = CalendarHandler.DEFAULT_DATE_FORMAT;
}
