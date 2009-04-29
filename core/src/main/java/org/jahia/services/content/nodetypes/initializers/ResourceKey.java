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

import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 11:17:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceKey implements ValueInitializer {

    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {

        String bundleId = ((ExtendedNodeType) declaringPropertyDefinition.getDeclaringNodeType()).getResourceBundleId();
        return new Value[] { new ValueImpl(
                ResourceBundleMarker.drawMarker(bundleId,declaringPropertyDefinition.getResourceBundleKey()+"."+params.get(0),params.get(0)),
                PropertyType.STRING, false) };
    }
}
