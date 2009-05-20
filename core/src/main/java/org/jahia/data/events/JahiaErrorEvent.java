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
package org.jahia.data.events;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents an event object for the <code>eventOccurred</code> Jahia event.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaErrorEvent extends JahiaEvent {

    private int errorCode;

    private Throwable exception;

    private transient HttpServletRequest request;

    private transient HttpServletResponse response;

    /**
     * Initializes an instance of this class.
     * 
     * @param source
     *            the source of the error
     * @param exception
     *            the occurred exception
     * @param errorCode
     *            the error code to be sent to the client
     * @param request
     *            current request object
     * @param response
     *            current response object
     */
    public JahiaErrorEvent(Object source, Throwable exception, int errorCode,
            HttpServletRequest request, HttpServletResponse response) {
        super(source, null, null, exception);
        this.exception = exception;
        this.errorCode = errorCode;
        this.request = request;
        this.response = response;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Throwable getException() {
        return exception;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

}
