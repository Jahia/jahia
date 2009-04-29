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
package org.jahia.exceptions;

/**
 * Jahia base class for runtime exceptions.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaRuntimeException extends RuntimeException {

    /**
     * Initializes an instance of this class.
     */
    public JahiaRuntimeException() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param message
     */
    public JahiaRuntimeException(String message) {
        super(message);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param message
     * @param cause
     */
    public JahiaRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param cause
     */
    public JahiaRuntimeException(Throwable cause) {
        super(cause);
    }

}
