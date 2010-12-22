package org.jahia.services.htmlvalidator;

import java.util.ArrayList;
import java.util.List;

public class ValidatorResults {
    private List<Result> errors = new ArrayList<Result>();
    private List<Result> warnings = new ArrayList<Result>();
    private List<Result> infos = new ArrayList<Result>();
    
    /**
     * Constructor for Result.
     */
    public ValidatorResults() {
            super();
    }
    
   
    /**
     * 
     * @return The list of errors.
     */
    public List<Result> getErrors() {
            return errors;
    }
    
    /**
     * 
     * @return The list of warnings.
     */
    public List<Result> getWarnings() {
            return warnings;
    }
    
    /**
     * 
     * @return The list of infos.
     */
    public List<Result> getInfos() {
            return infos;
    }
    
    /**
     * Adds an error
     * 
     */
    public boolean addError(Result result) {
        return errors.add(result);
    }
    
    /**
     * Adds a warning
     * 
     */
    public boolean addWarning(Result result) {
        return warnings.add(result);
    }
    
    /**
     * Adds an info
     * 
     */
    public boolean addInfo(Result result) {
        return infos.add(result);
    }
    
	public boolean isEmpty() {
		return errors.isEmpty() && warnings.isEmpty() && infos.isEmpty();
	}
}
