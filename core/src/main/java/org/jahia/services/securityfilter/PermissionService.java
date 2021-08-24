/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
