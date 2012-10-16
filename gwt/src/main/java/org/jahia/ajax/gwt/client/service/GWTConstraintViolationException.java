package org.jahia.ajax.gwt.client.service;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class GWTConstraintViolationException extends GWTJahiaServiceException implements Serializable, IsSerializable {
    private String uuid;
    private String constraintMessage;
    private String locale;
    private String propertyName;
    private String propertyLabel;

    public GWTConstraintViolationException() {
    }

    GWTConstraintViolationException(String uuid, String constraintMessage, String locale, String propertyName, String propertyLabel) {
        this.uuid = uuid;
        this.constraintMessage = constraintMessage;
        this.locale = locale;
        this.propertyName = propertyName;
        this.propertyLabel = propertyLabel;
    }

    public String getUuid() {
        return uuid;
    }

    public String getConstraintMessage() {
        return constraintMessage;
    }

    public String getLocale() {
        return locale;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyLabel() {
        return propertyLabel;
    }
}
