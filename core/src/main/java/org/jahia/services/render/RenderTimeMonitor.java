/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render;

import javax.servlet.http.HttpServletRequest;

import org.jahia.exceptions.RenderTimeLimitExceededException;

/**
 * Aimed to check the current request rendering time and, if it exceeds the maximum configured one, throws the
 * {@link RenderTimeLimitExceededException} to stop the current request processing chain.
 *
 * @author Sergiy Shyrkov
 */
public interface RenderTimeMonitor {

    /**
     * Checks the current request rendering time and, if it exceeds the maximum configured one, throws the
     * {@link RenderTimeLimitExceededException} to stop the current request processing chain.
     *
     * @param resource
     *            the currently rendered resource
     * @param renderContext
     *            current rendering context
     * @throws RenderTimeLimitExceededException
     *             in case the request rendering time exceeded the maximum allowed one
     */
    void monitor(Resource resource, RenderContext renderContext) throws RenderTimeLimitExceededException;

    /**
     * Start tracking the current request render time, if it was not done before. Subsequent calls for the current request are ignored.
     *
     * @param request
     *            the current HTTP request
     */
    void track(HttpServletRequest request);
}
