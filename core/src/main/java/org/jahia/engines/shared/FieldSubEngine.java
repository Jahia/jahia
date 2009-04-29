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
 package org.jahia.engines.shared;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: Jun 28, 2005
 * Time: 11:26:04 AM
 * To change this template use File | Settings | File Templates.
 */
public interface FieldSubEngine {
    boolean handleField( ProcessingContext jParams, Integer modeInt, Map<String, Object> engineMap )
    throws JahiaException;
}
