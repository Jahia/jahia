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

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;

public interface JahiaEventInterface {

    public abstract ProcessingContext getParams ();

    public abstract Object getObject ();

    public abstract long getEventTime ();

    public abstract JahiaData getJahiaData ();

}
