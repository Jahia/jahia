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

import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;

/**
 * <p>Title: Application dispatching interface.</p>
 * <p>Description: This interface defines the application-type neutral
* interface for dispatching.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */
public interface DispatchingProvider {


    public void start ()
        throws JahiaInitializationException;

    public void stop();

    /**
     * This method is called by Jahia when an application is asked to process
     * a request.
     * @param appBean ApplicationBean
     * @param windowID int
     * @param jParams ProcessingContext
     * @throws JahiaException
     */
    public void processAction(EntryPointInstance entryPointInstance, int windowID, ParamBean jParams)
        throws JahiaException;

    /**
     * This method is called by Jahia when an application is asked to render
     * it's output.
     * @param windowID int
     * @throws JahiaException
     * @return String
     */
    public String render (EntryPointInstance entryPointInstance, String windowID, ParamBean jParams)
        throws JahiaException;

}
