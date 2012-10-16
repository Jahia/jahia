package org.jahia.services.content;


import javax.jcr.nodetype.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeConstraintViolationException extends ConstraintViolationException {
    private List<ConstraintViolationException> errors = new ArrayList<ConstraintViolationException>();

    public CompositeConstraintViolationException() {
    }

    public void addException(ConstraintViolationException exception) {
        errors.add(exception);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolationException error : errors) {
            sb.append(error.getMessage());
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<ConstraintViolationException> getErrors() {
        return errors;
    }
}
