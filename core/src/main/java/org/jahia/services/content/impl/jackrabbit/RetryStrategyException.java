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

/**
 * Exception thrown by the {@link RetryOnExceptionStrategy} class when reaching the maximum number of retries
 *
 * @author bdjiba
 */
public class RetryStrategyException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8051361529656401391L;

    /**
     *
     */
    public RetryStrategyException() {
        /* EMPTY */
    }

    /**
     * Create a new Exception with associated message
     *
     * @param message Message associated with this exception
     */
    public RetryStrategyException(String message) {
        super(message);
    }

    /**
     * Create an Exception with the associated root cause
     *
     * @param cause The root cause for this Exception
     */
    public RetryStrategyException(Throwable cause) {
        super(cause);
    }

    /**
     * Create an Exception with the associated root cause and message
     *
     * @param message Message associated with this exception
     * @param cause   The root cause for this Exception
     */
    public RetryStrategyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create an Exception with the associated root cause and message and status
     *
     * @param message            Message associated with this exception
     * @param cause              The root cause for this Exception
     * @param enableSuppression  allow to suppress the Exception
     * @param writableStackTrace allow to rewrite the StackTrace
     */
    public RetryStrategyException(
            String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
