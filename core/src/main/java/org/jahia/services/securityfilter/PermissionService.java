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
package org.jahia.services.securityfilter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

/**
 * Content access check service.
 */
public interface PermissionService {


    /**
     * Get the list of available configured scopes
     * @return list of scopes
     */
    Collection<ScopeDefinition> getAvailableScopes();

    /**
     * Get the valid scope for the current request
     * @return list of scopes
     */
    Collection<ScopeDefinition> getCurrentScopes();

    /**
     * Set the scopes for the current thread. Override any previous value
     */
    void setCurrentScopes(Collection<ScopeDefinition> scopes);

    /**
     * Add scopes in the current request
     *
     * @param scopes Scope names to add
     * @param request Current request
     */
    void addScopes(Collection<String> scopes, HttpServletRequest request);

    /**
     * Initialize auto-applied scopes
     *
     * @param request Current request
     */
    void initScopes(HttpServletRequest request);

    /**
     * Reset scope at the end of the request
     */
    void resetScopes();

    /**
     * Checks if the current user has access to the specified API.
     *
     * @param query the object describing the api call
     * @return <code>true</code> if the current user is allowed to access the api; <code>false</code> otherwise
     * @throws RepositoryException in case of an error during permission check
     */
    public boolean hasPermission(Map<String, Object> query);

    /**
     * Checks if the current user has access to the specified API key.
     *
     * @param api the API key to test access
     * @return <code>true</code> if the current user is allowed to access the api; <code>false</code> otherwise
     * @throws RepositoryException in case of an error during permission check
     */
    public boolean hasPermission(String api) throws RepositoryException;

    /**
     * Checks if the current user has access to the specified node using the provided API key (used in configuration rules).
     *
     * @param api the API key to test access
     * @param node the requested JCR node, or null to test the permission globally
     * @return <code>true</code> if the current user is allowed to access the api; <code>false</code> otherwise
     * @throws RepositoryException in case of an error during permission check
     */
    public boolean hasPermission(String api, Node node) throws RepositoryException;
}
