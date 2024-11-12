/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
    @Override
    public String getName() {
        return NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean compare(Element element1, Element element2) {
        if(logger.isDebugEnabled()){
            logger.debug("Comparing desirableness of element: {} to element: {}", element1, element2);
        }
        return ((Set<String>) element1.getObjectValue()).contains(ALL) || !((Set<String>) element2.getObjectValue()).contains(ALL) && super.compare(element1, element2);
    }
}
