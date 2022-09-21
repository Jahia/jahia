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
package org.jahia.services.applications.pluto;

import org.jahia.data.applications.EntryPointInstance;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

/**
 * 
 * User: jahia
 * Date: 9 avr. 2009
 * Time: 17:34:03
 * 
 */
public class PortletWindowConfig {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PortletWindowConfig.class);

    public static String fromId(EntryPointInstance entryPointInstance) {
        final String defName = entryPointInstance.getDefName();
        final String windowID = entryPointInstance.getID();
        return (defName.startsWith(".") ? "/" : "") + defName + "!" + windowID;
    }

    public static String fromId(JCRPortletNode nodeWrapper) {
        try {
            final String contextName = nodeWrapper.getContextName();
            if(contextName==null) {
                return null;
            }
            EntryPointInstance entryPointInstance = new EntryPointInstance(nodeWrapper.getUUID(), contextName, nodeWrapper.getDefinitionName(), nodeWrapper.getName());
            return fromId(entryPointInstance);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
