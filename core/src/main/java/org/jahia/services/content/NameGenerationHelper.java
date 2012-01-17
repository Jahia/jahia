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

package org.jahia.services.content;

import org.jahia.services.content.nodetypes.ExtendedNodeType;

/**
 * Name generation helper class. You might want to implement this method to provide alternative name generation
 * schemes.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 5/4/11
 */
public interface NameGenerationHelper {

    /**
     * Basic name generation method, usually called by back-end methods or actions to generate a node name based
     * on the type
     * @param parent the parent object for which to create a new node name for a new child object
     * @param nodeType the node type of the new child object
     * @return a String containing the generated node name
     */
    String generatNodeName(JCRNodeWrapper parent, String nodeType);

    /**
     * Name generation method used mostly by Jahia's UI to generate node names based on the node type or the target
     * name. This method is passed the default language because it might use it to make resource bundle lookups to
     * generate the node name based on the node type.
     * The targetName is the name of the module that was specified in the view if it was set. This might be useful
     * to use as a node name in the case of more specific definitions
     * @param parent the parent object for which to create a new node name for a new child object
     * @param defaultLanguage the language code for the default language
     * @param nodeType the node type of the new child object
     * @param targetName the name of the module for the child node as specified in the definition (maybe be "*" if
     * any name is allowed or null if no targetName was specified at all).
     * @return a String containing the generated node name
     */
    String generatNodeName(JCRNodeWrapper parent, String defaultLanguage, ExtendedNodeType nodeType, String targetName);
}
