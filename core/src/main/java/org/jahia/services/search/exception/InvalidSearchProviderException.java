package org.jahia.services.search.exception;

/**
 * Created by kevan on 18/07/14.
 */
public class InvalidSearchProviderException extends Exception{
    private static final long serialVersionUID = -508286996136966851L;

    public InvalidSearchProviderException() {

    }

    public InvalidSearchProviderException(String message) {
        super(message);
    }
}
