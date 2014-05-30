package org.jahia.security.license;

public class LicenseCheckException extends Exception {
    public LicenseCheckException() {
    }

    public LicenseCheckException(String message) {
        super(message);
    }

    public LicenseCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public LicenseCheckException(Throwable cause) {
        super(cause);
    }
}
