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
 package org.jahia.services.applications;

import java.util.List;

import org.apache.pluto.PortletWindow;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;

public interface ApplicationsManagerProvider {
    /**
     * Creates an instance of an application entry point definition. This
     * does not need to create the entry in the database, just the memory
     * instance, which is dependent on the provider.
     * @param entryPointDefinition EntryPointDefinition
     * @throws JahiaException
     * @returns the created instance for the entry point definition.
     */
    public abstract EntryPointInstance createEntryPointInstance (
        EntryPointDefinition entryPointDefinition)
        throws JahiaException;

    public abstract PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID, ParamBean jParams);

    public List<EntryPointDefinition> getAppEntryPointDefinitions(ApplicationBean appBean)
        throws JahiaException;

    public void start()
        throws JahiaInitializationException;

    public void stop();

}
