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
 * Indicates malformed request or missing request parameter.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaBadRequestException extends JahiaRuntimeException {

    /**
     * Initializes an instance of this class.
     * 
     * @param message
     */
    public JahiaBadRequestException(String message) {
        super(message);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param message
     * @param cause
     */
    public JahiaBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param cause
     */
    public JahiaBadRequestException(Throwable cause) {
        super(cause);
    }

}
