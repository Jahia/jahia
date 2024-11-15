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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import net.sf.ehcache.pool.SizeOfEngine;
import net.sf.ehcache.pool.SizeOfEngineFactory;
import net.sf.ehcache.pool.impl.DefaultSizeOfEngine;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.slf4j.Logger;

public class JahiaSizeOfEngineFactory implements SizeOfEngineFactory {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaSizeOfEngineFactory.class);
    @Override
    public SizeOfEngine createSizeOfEngine(int maxObjectCount, boolean abort, boolean silent) {
        logger.info("Using JahiaSizeOfEngineFactory");
        System.setProperty(DefaultSizeOfEngine.USER_FILTER_RESOURCE,Thread.currentThread().getContextClassLoader().getResource("jahiaSizeOfEngine.filter").toString());
        return new DefaultSizeOfEngine(maxObjectCount, abort, silent);
    }
}
