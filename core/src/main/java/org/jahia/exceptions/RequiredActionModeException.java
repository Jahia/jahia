package org.jahia.exceptions;

import javax.jcr.AccessDeniedException;

public class RequiredActionModeException extends AccessDeniedException {

    public RequiredActionModeException(String message) {
        super(message);
    }
}
