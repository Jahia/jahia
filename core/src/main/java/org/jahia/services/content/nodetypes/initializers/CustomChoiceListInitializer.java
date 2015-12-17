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
 *     This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import javax.jcr.*;

/**
 * @author : faissah
 * Choicelist initializer that allows to you to add/remove mixins depending on the selected value.
 *
 * In its Spring configuration, this choicelist initializer is configured with a list of CustomChoiceListInitializerItem
 * where you where you set the choicelist values and the mixins associated.
 *
 */
public class CustomChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(CustomChoiceListInitializer.class);

    private String key;
    private List<CustomChoiceListInitializerItem> items = new ArrayList<CustomChoiceListInitializerItem>();

    /**
     * {@inheritDoc}
     */
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values,
                                                     Locale locale, Map<String, Object> context) {

        final List<ChoiceListValue> myChoiceList = new ArrayList<ChoiceListValue>();

        if (context == null) {
            return myChoiceList;
        }

        HashMap<String, Object> myPropertiesMap = null;

        for (CustomChoiceListInitializerItem item : items) {
            myPropertiesMap = new HashMap<String, Object>();
            if (item.getMixin()!=null)
                myPropertiesMap.put("addMixin", item.getMixin());
            myChoiceList.add(new ChoiceListValue(item.getDisplayName(), myPropertiesMap, new ValueImpl(item.getValue(), PropertyType.STRING, false)));
        }

        //Return the list
        return myChoiceList;
    }

    /**
     * {@inheritDoc}
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    public String getKey() {
        return key;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public List getItems() {
        return items;
    }
}

