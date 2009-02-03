package org.jahia.services.search.lucene;

import java.io.Serializable;

import org.jahia.hibernate.model.indexingjob.JahiaIndexingJob;

/**
 * used as SearchClusterMessage to notify Synchronized Indexation event
 */
public class SynchronizedIndexationRequestMessage implements Serializable {

    private static final long serialVersionUID = 8552979623524155037L;
    private JahiaIndexingJob indexingJob;

    /**
     *
     * @param indexingJob
     */
    public SynchronizedIndexationRequestMessage(JahiaIndexingJob indexingJob) {
        super();
        this.indexingJob = indexingJob;
    }

    public JahiaIndexingJob getIndexingJob() {
        return indexingJob;
    }

    public void setIndexingJob(JahiaIndexingJob indexingJob) {
        this.indexingJob = indexingJob;
    }

}