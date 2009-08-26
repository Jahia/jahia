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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;
import org.jahia.utils.i18n.ResourceBundleMarker;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.PropertyType;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 19 févr. 2009
 * Time: 15:08:07
 * To change this template use File | Settings | File Templates.
 */
public class ResourceBundle implements ValueInitializer {
    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params, Map context) {
        Locale currentLocale = Locale.getDefault();
        if (jParams != null) {
            currentLocale = jParams.getCurrentLocale();
        }
        final String bundleName = params.get(0);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle(bundleName,
                currentLocale, JahiaTemplatesRBLoader.getInstance(this.getClass().getClassLoader(),
                        declaringPropertyDefinition.getDeclaringNodeType().getSystemId()));
        SortedSet<Value> values = new TreeSet<Value>(new Comparator<Value>() {
            public int compare(Value o, Value o1) {
                try {
                    return o.getString().compareTo(o1.getString());
                } catch (RepositoryException e) {
                    return -1;
                }
            }
        });
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            values.add(new ValueImpl(ResourceBundleMarker.drawMarker(bundleName,key,key), PropertyType.STRING,false));
        }
        return values.toArray(new Value[values.size()]);
    }
}
