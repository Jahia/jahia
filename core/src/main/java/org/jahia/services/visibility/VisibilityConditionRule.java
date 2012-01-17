/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
