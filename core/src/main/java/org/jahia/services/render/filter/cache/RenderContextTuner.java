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

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * This interface can be implemented by a CacheKeyPartGenerator to be able to hook before and after content generation in aggregation,
 * Sometimes we store data in the key, and we want to re-inject this data before sub-fragment generation, and clean this re-injected
 * data after the fragment generation. This interface is used for that.
 * <p>
 * @deprecated Render chain V2 is deprecated and will be removed in the next major release (8.3.0.0).
 * <p>
 * Created by jkevan on 08/04/2017.
 */
@Deprecated(since = "8.2.1.0", forRemoval = true)
public interface RenderContextTuner {

    /**
     * This will be called before a new RenderChain is started to generate a sub-fragment.
     *
     * This allows to modify the context, or store data in request that need be reused for the sub-fragment generation.
     * This is the case for module parameters, node type restrictions coming from the parent area, etc.
     *
     * @param value the value parsed from the key by this key part generator
     * @param resource the current resource rendered
     * @param renderContext the current renderContext
     * @return An object that represents the original state of the key part in the render context
     * <p>
     * @deprecated Render chain V2 is deprecated and will be removed in the next major release (8.3.0.0).
     * <p>
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    Object prepareContextForContentGeneration(String value, Resource resource, RenderContext renderContext);

    /**
     * This will be called after a RenderChain finished to generate a sub-fragment.
     *
     * This allows to restore the context, request or anything that have been modified in the "prepareContextForContentGeneration".
     *
     * @param value the value parsed from the key by this key part generator
     * @param resource the current resource rendered
     * @param renderContext the current renderContext
     * @param original the original object previously retrieved via prepareContextForContentGeneration() invocation
     * <p>
     * @deprecated Render chain V2 is deprecated and will be removed in the next major release (8.3.0.0).
     * <p>
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    void restoreContextAfterContentGeneration(String value, Resource resource, RenderContext renderContext, Object original);
}
