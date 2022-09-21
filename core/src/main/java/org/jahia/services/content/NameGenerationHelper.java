/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
