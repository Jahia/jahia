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