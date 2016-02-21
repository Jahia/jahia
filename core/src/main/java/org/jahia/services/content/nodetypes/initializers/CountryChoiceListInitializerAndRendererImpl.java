/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.renderer.AbstractChoiceListRenderer;
import org.jahia.services.render.RenderContext;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * An implementation for choice list initializer and renderer that displays a list of available countries.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class CountryChoiceListInitializerAndRendererImpl extends AbstractChoiceListRenderer implements ChoiceListInitializer {
    
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param,
                                                     List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        String[] iso = Locale.getISOCountries();
        List<ChoiceListValue> l = new ArrayList<ChoiceListValue>(iso.length);
        for (String anIso : iso) {
            l.add(new ChoiceListValue(new Locale("en", anIso).getDisplayCountry(locale), null,
                                      new ValueImpl(anIso, PropertyType.STRING, false)));
        }
        Collections.sort(l);
        return l;
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
    
    public String getStringRendering(Locale locale, ExtendedPropertyDefinition propDef, Object propertyValue) throws RepositoryException {
        return new Locale("en", propertyValue.toString()).getDisplayCountry(
                locale);
    }  
}
