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
package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * This interface can be implemented by a CacheKeyPartGenerator to be able to hook before and after content generation in aggregation,
 * Sometimes we store data in the key, and we want to re-inject this data before sub-fragment generation, and clean this re-injected
 * data after the fragment generation. This interface is used for that.
 *
 * Created by jkevan on 08/04/2017.
 */
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
     */
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
     */
    void restoreContextAfterContentGeneration(String value, Resource resource, RenderContext renderContext, Object original);
}
