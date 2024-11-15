/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
