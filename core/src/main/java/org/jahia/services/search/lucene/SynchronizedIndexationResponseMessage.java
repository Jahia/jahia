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
package org.jahia.services.search.lucene;

import java.io.Serializable;

import org.jahia.hibernate.model.indexingjob.JahiaIndexingJob;

/**
 * used as SearchClusterMessage to notify Synchronized Indexation event
 */
public class SynchronizedIndexationResponseMessage implements Serializable {

    private static final long serialVersionUID = 6792564094362356347L;
    private String serverId;
    private JahiaIndexingJob indexingJob;

    /**
     * 
     * @param indexingJob
     * @param serverId
     */
    public SynchronizedIndexationResponseMessage(JahiaIndexingJob indexingJob,
                                                 String serverId) {
        super();
        this.serverId = serverId;
        this.indexingJob = indexingJob;
    }

    public JahiaIndexingJob getIndexingJob() {
        return indexingJob;
    }

    public void setIndexingJob(JahiaIndexingJob indexingJob) {
        this.indexingJob = indexingJob;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

}