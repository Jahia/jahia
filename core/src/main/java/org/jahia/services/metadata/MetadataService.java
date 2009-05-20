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
//
package org.jahia.services.metadata;

import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ObjectKey;
import org.jahia.data.events.JahiaEventListenerInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

/**
 * Metadata Service
 *
 * @author Khue Nguyen
 */
public abstract class MetadataService extends JahiaService
{

    /**
     * Reload configuration file from disk
     *
     * @throws JahiaException
     */
    public abstract void reloadConfigurationFile()
    throws java.io.FileNotFoundException, JahiaException;

    /**
     * Return the JahiaEventListener used to handle Metadata
     *
     */
    public abstract JahiaEventListenerInterface getMetadataEventListener();

    /**
     * Add an aopalliance interceptor to the JahiaEventListener
     *
     */
    public abstract void addAOPInterceptor(Interceptor interceptor);

    /**
     * Returns an array list of metadata that match this contentDefinition
     *
     * @param contentDefinition ContentDefinition
     * @return boolean
     */
    public abstract List getMatchingMetadatas(ContentDefinition contentDefinition);

    /**
     * Returns true if the given field definition is declared in metadata config file
     * @param name the metadata name ( jahia field definition name )
     * @return
     */
    public abstract boolean isDeclaredMetadata(String name);

    /**
     * Create all metadata associations between a ContentDefinition and
     * registered Metadata Definitions
     *
     * @param contentDefinition ContentDefinition
     * @throws JahiaException
     */
    public abstract void assignMetadataToContentDefinition(ContentDefinition contentDefinition)
    throws JahiaException;

    /**
     * Returns an array of ObjectKey that are metadatas
     *
     * @param name String, the metadata name
     * @throws JahiaException
     * @return ArrayList
     */
    public abstract List<ObjectKey> getMetadataByName(String name)
    throws JahiaException;

    /**
     * Returns an array of ObjectKey that are metadatas of a given site
     *
     * @param name String, the metadata name
     * @param siteId, the site id
     * @throws JahiaException
     * @return ArrayList
     * @throws JahiaException
     */
    public abstract List<ObjectKey> getMetadataByName(String name, int siteId)
    throws JahiaException;

}

