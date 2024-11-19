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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
