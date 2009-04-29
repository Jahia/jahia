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

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 19 févr. 2009
 * Time: 14:05:57
 * To change this template use File | Settings | File Templates.
 */
public class Country implements ValueInitializer {
    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        final Locale[] availableLocales = Locale.getAvailableLocales();
        SortedSet<Value> values = new TreeSet<Value>(new Comparator<Value>() {
            public int compare(Value o, Value o1) {
                try {
                    return o.getString().compareTo(o1.getString());
                } catch (RepositoryException e) {
                    return -1;
                }
            }
        });
        int i =0;
        for (Locale locale : availableLocales) {
            final String country;
            if (jParams != null)
                country = locale.getDisplayCountry(jParams.getCurrentLocale());
            else country = locale.getDisplayCountry();
            if (country.length() > 0) {
                values.add(new ValueImpl(country, PropertyType.STRING,false));
            }
        }
        return values.toArray(new Value[values.size()]);
    }
}
