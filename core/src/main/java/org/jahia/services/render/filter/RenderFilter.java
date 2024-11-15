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
package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Interface that defines a filter usable in the {@link RenderChain}.
 *
 * Each filter can either call the next filter and transform the output, or generate its own output. It can execute
 * operations before/after calling the next filter.
 *
 * Date: Nov 24, 2009
 * Time: 12:08:45 PM
 */
public interface RenderFilter extends RenderServiceAware, Comparable<RenderFilter> {

    /**
     * Get the priority number of the filter. Filter will be executed in order of priority, lower first.
     */
    float getPriority();

    public boolean areConditionsMatched(RenderContext renderContext, Resource resource);

    String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception;

    String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception;

    String getContentForError(RenderContext renderContext, Resource resource, RenderChain renderChain, Exception e);

    void finalize(RenderContext renderContext, Resource resource, RenderChain renderChain);

    /**
     * Get the name of the current render filter instance
     * @return The name of the filter
     */
    default String getName() {
        return null;
    }

    /**
     * Get the description of the current render filter instance
     * @return The description of the filter
     */
    default String getDescription() {
        return null;
    }

    /**
     * Check if the filter is disabled or not.
     * @return true if the filter is disabled
     */
    default boolean isDisabled() {
        return false;
    }

    /**
     * Get a summary of all conditions applied to the current render filter
     * @return the human-readable summary of the conditions
     */
    default String getConditionsSummary() {
        return null;
    }

    /**
     * Disable or enable current render filter
     * @param disabled true to disable the filter, false to enable it
     */
    default void setDisabled(boolean disabled) {
        throw new UnsupportedOperationException("Please extends AbstractFilter class to be able to enable/disable the instance");
    }

    /**
     * Set the priority of the current render filter instance
     * @param priority the new priority to be applied
     */
    default void setPriority(float priority) {
        throw new UnsupportedOperationException("Please extends AbstractFilter class to be able to override the filter priority");
    }
}
