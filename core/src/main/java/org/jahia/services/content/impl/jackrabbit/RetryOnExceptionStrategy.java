/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jackrabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry strategy on cluster sync when exception occur
 *
 * @author bdjiba
 */
public class RetryOnExceptionStrategy {
    private static final Logger log = LoggerFactory.getLogger(RetryOnExceptionStrategy.class);
    /**
     * Default delay time between retries in ms
     */
    public static final long DEFAULT_WAIT_TIME_IN_MILLIS = 500;
    /**
     * Default max retries on Exception
     */
    public static final int DEFAULT_MAX_RETIRES = 3;

    /**
     * Number of attempts
     */
    private int retries;
    /**
     * Time to observe until tentative
     */
    private long timeToWait;
    /**
     * Number of remaining tries
     */
    private int triesLeft;


    /**
     * Constructor that uses default values
     */
    public RetryOnExceptionStrategy() {
        this(DEFAULT_MAX_RETIRES, DEFAULT_WAIT_TIME_IN_MILLIS);
    }

    /**
     * Constructor with custom retry strategy attribute
     *
     * @param maxRetries    the max retry count
     * @param intervalDelay the observation delay
     */
    public RetryOnExceptionStrategy(int maxRetries, long intervalDelay) {
        this.retries = maxRetries;
        this.triesLeft = maxRetries;
        this.timeToWait = intervalDelay;
    }

    /**
     * Get the time (in ms) between retries
     *
     * @return the time (in ms) between retries
     */
    public long getTimeToWait() {
        return timeToWait;
    }

    public int getTriesLeft() {
        return triesLeft;
    }

    public int getRetries() {
        return retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * @param timeToWait the timeToWait to set
     */
    public void setTimeToWait(long timeToWait) {
        this.timeToWait = timeToWait;
    }

    /**
     * @param triesLeft the triesLeft to set
     */
    public void setTriesLeft(int triesLeft) {
        this.triesLeft = triesLeft;
    }

    /**
     * Can e still retry?
     * @return true if there is still tries available
     */
    public boolean canRetry() {
        return triesLeft > 0;
    }

    private void waitUntilNextRetry() {
        try {
            log.info("Going to wait {}", timeToWait);
            Thread.sleep(timeToWait);
        } catch (InterruptedException iex) {
            log.trace(iex.getMessage(), iex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * An error occurred retry sync
     * @throws RetryStrategyException no more retry available stop process
     */
    public void onErrorOccured() throws RetryStrategyException {
        --triesLeft;
        if (!canRetry()) {
            throw new RetryStrategyException("Failed: " + retries + " attempts reached at interval " + timeToWait + " ms.");
        }
        // then observe delay
        waitUntilNextRetry();
    }

}
