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
package org.jahia.services.search;

import org.jahia.hibernate.model.indexingjob.JahiaIndexingJob;
import org.jahia.services.search.lucene.SynchronizedIndexationResponseMessage;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 3 avr. 2008
 * Time: 12:25:14
 * To change this template use File | Settings | File Templates.
 */
public class SynchronizedIndexationTask {

    private static final transient Logger logger = Logger.getLogger(SynchronizedIndexationTask.class);

    private JahiaIndexingJob indexinJob;
    private String indexServerID;
    private long waitMaxTime;
    private boolean taskIsPending = true;
    private CountDownLatch task;

    /**
     *
     * @param indexingJob
     * @param indexServerID
     * @param waitMaxTime
     */
    public SynchronizedIndexationTask(JahiaIndexingJob indexingJob,
                                      String indexServerID,
                                      long waitMaxTime) {
        this.indexinJob = indexingJob;
        this.indexServerID = indexServerID;
        this.waitMaxTime = waitMaxTime;
        this.task = new CountDownLatch(1);
    }

    public void startWait(){
        try {
            this.task.await(waitMaxTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e){
            logger.debug(e);
        }
        this.taskIsPending = false;
    }

    public void messageReceived(SynchronizedIndexationResponseMessage msg) {
        JahiaIndexingJob otherJob = msg.getIndexingJob();
        if (this.indexServerID.equals(msg.getServerId())
                && this.indexinJob.getClassName().equals(otherJob.getClassName())
                && this.indexinJob.getDate().equals(otherJob.getDate())){
            this.task.countDown();
        }
    }

    public boolean isTaskIsPending() {
        return taskIsPending;
    }

    public void setTaskIsPending(boolean taskIsPending) {
        this.taskIsPending = taskIsPending;
    }

}
