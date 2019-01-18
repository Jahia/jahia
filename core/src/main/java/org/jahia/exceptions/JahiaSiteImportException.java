package org.jahia.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class JahiaSiteImportException extends Exception implements Serializable, IsSerializable {

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
