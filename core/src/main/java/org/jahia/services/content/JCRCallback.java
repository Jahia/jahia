/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import javax.jcr.RepositoryException;

/**
 * Callback interface for JCR code. To be used with JCRTemplate's execute methods,
 * assumably often as anonymous classes within a method implementation. 
 *
 * @author Cedric Mailleux
 */

public interface JCRCallback<T> {

    /**
     * Called by {@link JCRTemplate} within an active JCR
     * {@link javax.jcr.JCRSession}. It is not responsible for logging
     * out of the <code>Session</code> or handling transactions.
     *
     * Allows for returning a result object created within the
     * callback, i.e. a domain object or a collection of domain
     * objects. A thrown {@link RuntimeException} is treated as an
     * application exception; it is propagated to the caller of the
     * template.
     * 
     * @param session
     *            session passed by the JCRTemplate 
     * @return a result object returned by the action, or null
     * @throws RepositoryException in case of JCR errors
     */
    T doInJCR(JCRSessionWrapper session) throws RepositoryException;
}
