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
package org.apache.jackrabbit.core.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry callback a specific amount of times, waiting between every execution
 */
public class Retryier {

    private static final Logger logger = LoggerFactory.getLogger(Retryier.class);

    private final int max;
    private final int wait;
    private final double waitMultiplier;

    public Retryier(int max, int wait, double waitMultiplier) {
        this.max = max;
        this.wait = wait;
        this.waitMultiplier = waitMultiplier;
    }

    public boolean retry(Call<?> action, Call<?> doBeforeRetry) throws RetryException {
        int count = 0;
        int currentWait = this.wait;
        Exception thrown;
        do {
            try {
                if (count > 0 && doBeforeRetry != null) {
                    doBeforeRetry.apply();
                }

                action.apply();
                return true;
            } catch (Exception e) {
                thrown = e;
                logger.debug("Failed {} on {}", count, max, e);
            }
            try {
                Thread.sleep(currentWait);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            count++;
            currentWait = (int) (currentWait * waitMultiplier);
        } while (count < max);

        throw new RetryException(thrown);
    }

    @FunctionalInterface
    public interface Call<T extends Exception> {
        void apply() throws T;
    }

    public static class RetryException extends Exception {
        public RetryException(Throwable cause) {
            super(cause);
        }

        @Override
        public synchronized Exception getCause() {
            return (Exception) super.getCause();
        }
    }

}
