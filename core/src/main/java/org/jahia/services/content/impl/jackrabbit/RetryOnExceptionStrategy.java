/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
