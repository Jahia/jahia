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
package org.jahia.services.content.nodetypes.initializers;

import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.apache.jackrabbit.util.ISO8601;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 11:09:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class Now implements ValueInitializer {
    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        return new Value[] { new ValueImpl(ISO8601.format(new GregorianCalendar()), PropertyType.DATE, false) };        
    }
}
