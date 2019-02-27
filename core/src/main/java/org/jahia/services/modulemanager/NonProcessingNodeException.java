package org.jahia.services.modulemanager;

/**
 * Indicates an attempt to perform an operation which is only allowed on the processing node, on a non-processing one.
 */
public class NonProcessingNodeException extends ModuleManagementException {

    private static final long serialVersionUID = -7211008750437320009L;

    /**
     * Create an exception instance.
     */
    public NonProcessingNodeException() {
        this("The operation can only be performed on the processing node");
    }

    /**
     * Create an exception instance.
     *
     * @param message Error message
     */
    public NonProcessingNodeException(String message) {
        super(message);
    }
}
