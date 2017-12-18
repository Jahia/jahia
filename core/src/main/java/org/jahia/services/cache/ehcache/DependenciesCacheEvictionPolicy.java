/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.Element;
import net.sf.ehcache.store.LruPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Implements a specific eviction policy for dependencies, extending LruPolicy from EHCache, which has a special handling for ALL
 * dependency.
 *
 * @author rincevent
 */
public class DependenciesCacheEvictionPolicy extends LruPolicy {
    public static final String ALL = "ALL";
    private static Logger logger = LoggerFactory.getLogger(DependenciesCacheEvictionPolicy.class);

    /**
     * The name of this policy as a string literal
     */
    public static final String NAME = "DependenciesAllBeforeLRU";

    /**
     * @return the name of the Policy. Inbuilt examples are LRU, LFU and FIFO.
     */
    public String getName() {
        return NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean compare(Element element1, Element element2) {
        if(logger.isDebugEnabled()){
            logger.debug("Comparing desirableness of element:\n"+element1+"\nto element:\n"+element2);
        }
        return ((Set<String>) element1.getObjectValue()).contains(ALL) || !((Set<String>) element2.getObjectValue()).contains(ALL) && super.compare(element1, element2);
    }
}
