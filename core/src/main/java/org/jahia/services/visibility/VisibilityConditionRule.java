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
