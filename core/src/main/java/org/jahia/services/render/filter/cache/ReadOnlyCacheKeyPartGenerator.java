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
package org.jahia.services.render.filter.cache;

import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.settings.readonlymode.ReadOnlyModeController;

/*
 * Cache key used to take into account the full read-only mode
 */
public class ReadOnlyCacheKeyPartGenerator implements CacheKeyPartGenerator {

    public static final String CACHE_READ_ONLY = "cache.readOnly";
    public static final String READ_ONLY = "read_only";
    public static final String READ_ONLY_KEY = "_ro_";

    @Override
    public String getKey() {
        return READ_ONLY;
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {

        if (Boolean.TRUE.toString().equals(properties.getProperty(CACHE_READ_ONLY))) {
            return READ_ONLY_KEY;
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        final String readOnlyStatus = ReadOnlyModeController.getInstance().getReadOnlyStatus().name();
        return StringUtils.replace(keyPart, READ_ONLY_KEY, readOnlyStatus);
    }

    @Override
    public ClientCachePolicy getClientCachePolicy(Resource resource, RenderContext renderContext, Properties properties, String key) {
        return ClientCachePolicy.DEFAULT;
    }
}
