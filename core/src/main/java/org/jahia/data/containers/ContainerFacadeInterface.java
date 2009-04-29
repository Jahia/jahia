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
package org.jahia.data.containers;

import java.util.Iterator;
import java.util.Map.Entry;

import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Used to hold a set of JahiaContainer instance in multiple language.
 *
 * @author Khue Nguyen
 */
public interface ContainerFacadeInterface {

    //--------------------------------------------------------------------------
    public abstract Iterator<JahiaContainer> getContainers();

    //--------------------------------------------------------------------------
    public abstract Iterator<JahiaContentFieldFacade> getFields();

    //--------------------------------------------------------------------------
    public abstract JahiaContentFieldFacade getContentFieldFacade(int fieldID);

    //--------------------------------------------------------------------------
    /**
     * Return a container for a entryLoadRequest using resolve entry state mechanism.
     *
     * @param EntryLoadRequest entryLoadRequest
     * @param boolean stagingIfActiveNotFound
     */
    public abstract JahiaContainer getContainer( EntryLoadRequest entryLoadRequest,
                                        boolean activeIfStagingNotFound );

    //--------------------------------------------------------------------------
    /**
     * Change the field type ( from undefined to another type )
     *
     * @param type
     */
    public abstract void changeType(int fieldId, int type,
            ProcessingContext jParams) throws JahiaException;
}
