/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
