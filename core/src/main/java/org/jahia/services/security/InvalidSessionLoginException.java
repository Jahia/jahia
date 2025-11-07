package org.jahia.services.security;

import org.jahia.services.content.decorator.JCRUserNode;

import javax.security.auth.login.LoginException;

/**
 * Based on <code>j:invalidateSessionTime</code> property.
 *
 * @see JCRUserNode#getInvalidatedSessionTime()
 */
public class InvalidSessionLoginException extends LoginException {
    public InvalidSessionLoginException() {
        super("The session used to login is invalid.");
    }
}
