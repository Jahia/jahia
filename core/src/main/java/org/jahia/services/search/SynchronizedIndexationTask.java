/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
