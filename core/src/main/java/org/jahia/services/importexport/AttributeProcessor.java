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
package org.jahia.services.importexport;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeWrapper;

/**
 * Instances implementing this interface are used during JCR import to process attributes of each element in the imported XML content, thus
 * providing special handling for particular attributes.
 */
public interface AttributeProcessor {
    /**
     * Does a special processing of the specified attribute.
     * 
     * @param node
     *            the current JCR node we are applying attribute value to
     * @param name
     *            the name of the attribute
     * @param value
     *            the attribute value
     * @return <code>true</code> if the processing of the attribute is effectively done should be stopped, i.e. no further
     *         {@link AttributeProcessor}s should be called for this attribute and no standard processing should be done for it;
     *         <code>false</code> in case the attribute does not have special handling by this processor and its handling should be
     *         continued further
     * @throws RepositoryException
     *             in case of any JCR or processing error
     */
    boolean process(JCRNodeWrapper node, String name, String value) throws RepositoryException;
}
