package org.jahia.services.security;

import javax.security.auth.login.LoginException;

public class ConcurrentLoggedInUsersLimitExceededLoginException extends LoginException {
    public ConcurrentLoggedInUsersLimitExceededLoginException() {
        super("The number of logged in users has reached the authorized limit.");
    }
}
