/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

/**
 * Choice list initializer that uses predefined list of choice options.
 * 
 * @author Sergiy Shyrkov
 */
public class FixedListChoiceListInitializer implements ModuleChoiceListInitializer {

    /**
     * The list of predefined choice options.
     */
    private List<ChoiceListValue> items;

    /**
     * The choice list initializer unique key.
     */
    private String key;

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
            List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        if (items == null || items.size() == 0) {
            return Collections.emptyList();
        }
        List<ChoiceListValue> itemsCopy = new LinkedList<>();
        for (ChoiceListValue value : items) {
            itemsCopy.add(new ChoiceListValue(value.getDisplayName(), value.getProperties(), value.getValue()));
        }
        
        return itemsCopy;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * Injects the list of predefined choice options.
     * 
     * @param items
     *            list of predefined choice options
     */
    public void setItems(List<ChoiceListValue> items) {
        this.items = items;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

}
