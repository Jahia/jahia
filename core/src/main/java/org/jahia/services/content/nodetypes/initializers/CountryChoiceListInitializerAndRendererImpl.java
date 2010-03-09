/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.render.RenderContext;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * An implementation for choice list initializer and renderer that displays a list of available countries.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class CountryChoiceListInitializerAndRendererImpl implements ChoiceListInitializer, ChoiceListRenderer {
    
    public List<ChoiceListValue> getChoiceListValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition,
                                                     ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values) {
        String[] iso = Locale.getISOCountries();
        List<ChoiceListValue> l = new ArrayList<ChoiceListValue>(iso.length);
        for (String anIso : iso) {
            l.add(new ChoiceListValue(new Locale("en", anIso).getDisplayCountry(jParams.getLocale()), null,
                                      new ValueImpl(anIso, PropertyType.STRING, false)));
        }
        Collections.sort(l);
        return l;
    }

    public Map<String,Object> getObjectRendering(RenderContext context, JCRPropertyWrapper propertyWrapper) throws RepositoryException {
        Map<String, Object> map = new HashMap<String, Object>(1);
        map.put("displayName", getStringRendering(context, propertyWrapper));
        return map;
    }

    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper) throws RepositoryException {
        String rendering;
        if(propertyWrapper.isMultiple()) {
            rendering = new Locale("en", propertyWrapper.getValues()[0].getString()).getDisplayCountry(
                context.getMainResource().getLocale());
        } else {
            rendering = new Locale("en", propertyWrapper.getValue().getString()).getDisplayCountry(
                context.getMainResource().getLocale());
        }
        return rendering;
    }
}
