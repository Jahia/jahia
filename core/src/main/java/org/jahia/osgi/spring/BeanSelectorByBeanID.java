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
package org.jahia.osgi.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

/**
 * Selects beans with specific bean IDs.
 */
public class BeanSelectorByBeanID implements SpringBridge.BeanSelector {

    private Collection<String> beanIDs;

    /**
     * Create an instance that will select a single bean by its ID.
     * @param beanID Spring bean ID
     */
    public BeanSelectorByBeanID(String beanID) {
        this.beanIDs = Collections.singleton(beanID);
    }

    /**
     * Create an instance that will select multiple beans by their IDs.
     * @param beanIDs Spring bean IDs
     */
    public BeanSelectorByBeanID(Collection<String> beanIDs) {
        this.beanIDs = new ArrayList<String>(beanIDs);
    }

    @Override
    public Map<String, Object> selectBeans(ApplicationContext applicationContext) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(beanIDs.size());
        for (String beanID : beanIDs) {
            Object bean = applicationContext.getBean(beanID);
            result.put(beanID, bean);
        }
        return result;
    }
}
