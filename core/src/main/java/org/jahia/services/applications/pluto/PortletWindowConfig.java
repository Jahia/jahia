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
