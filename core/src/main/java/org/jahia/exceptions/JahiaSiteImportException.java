package org.jahia.exceptions;

import java.io.Serializable;

/**
 * Thrown when there are errors during manual (via imports folder) site import
 *
 * @author alexanderkarmanov
 */
public class JahiaSiteImportException extends Exception implements Serializable {

    public JahiaSiteImportException() {
        super();
    }

    public JahiaSiteImportException(String message) {
        super(message);
    }

    public JahiaSiteImportException(Throwable cause) {
        super(cause);
    }

    public JahiaSiteImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
