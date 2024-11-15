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
package org.jahia.services.visibility;

import org.jahia.services.content.JCRNodeWrapper;

import java.util.List;
import java.util.Locale;

/**
 * Defines a visibility condition for a piece of content in Jahia.
 * @author rincevent
 * @since JAHIA 6.6
 * Created : 8/29/11
 */
public interface VisibilityConditionRule {

    /**
     * Returns <code>true</code> if the condition is satisfied and content will be rendered.
     *
     * @param node
     *            the node to test visibility condition for
     * @return <code>true</code> if the condition is satisfied and content will be rendered
     */
    boolean matches(JCRNodeWrapper node);

    /**
     * Return the node type associated with this condition.
     *
     * @return Return the node type associated with this condition.
     */
    String getAssociatedNodeType();

    /**
     * Return the associated display template that will be used by gwt.
     *
     * @return Return the associated display template that will be used by gwt.
     */
    String getGWTDisplayTemplate(Locale locale);

    /**
     * Returns a list of field names, required to display the info.
     *
     * @return a list of field names, required to display the info
     */
    List<String> getRequiredFieldNamesForTemplate();
}
