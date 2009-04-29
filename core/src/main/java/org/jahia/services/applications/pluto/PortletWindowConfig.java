/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.applications.pluto;

import org.jahia.data.applications.EntryPointInstance;
import org.jahia.services.content.JCRPortletNode;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 9 avr. 2009
 * Time: 17:34:03
 * To change this template use File | Settings | File Templates.
 */
public class PortletWindowConfig {

    public static String fromId(EntryPointInstance entryPointInstance) {
        final String defName = entryPointInstance.getDefName();
        final String windowID = entryPointInstance.getID();
        return (defName.startsWith(".") ? "/" : "") + defName + "!" + windowID;
    }

    public static String fromId(JCRPortletNode nodeWrapper) {
        try {
            EntryPointInstance entryPointInstance = new EntryPointInstance(nodeWrapper.getUUID(), nodeWrapper.getContextName(), nodeWrapper.getDefinitionName(), nodeWrapper.getName());
            return fromId(entryPointInstance);
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }

}
