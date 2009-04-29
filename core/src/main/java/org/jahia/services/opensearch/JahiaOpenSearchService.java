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
package org.jahia.services.opensearch;

import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

/**
 * OpenSearch Service
 *
 * @author Khue Nguyen
 */
public abstract class JahiaOpenSearchService extends JahiaService
{
    /**
     * Reload configuration file from disk
     *
     * @throws org.jahia.exceptions.JahiaException
     */
    public abstract void loadSearchEngines() throws JahiaException;

    /**
     *
     * @return
     * @throws JahiaException
     */
    public abstract List<SearchEngineBean> getSearchEngines() throws JahiaException;

    /**
     *
     * @return
     * @throws JahiaException
     */
    public abstract List<SearchEngineGroupBean> getSearchEngineGroups() throws JahiaException;

}