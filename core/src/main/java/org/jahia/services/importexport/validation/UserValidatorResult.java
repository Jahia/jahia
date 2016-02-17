package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * User validation result
 */
public class UserValidatorResult implements ValidationResult, Serializable {

    private Set<String> duplicateUsers = new TreeSet<String>();

    public UserValidatorResult(Set<String> duplicateUsers) {
        this.duplicateUsers = duplicateUsers;
    }

    public Set<String> getDuplicateUsers() {
        return duplicateUsers;
    }

    @Override
    public boolean isSuccessful() {
        return duplicateUsers.isEmpty();
    }

    @Override
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith;
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    public String getMessageKey() {
        return "failure.import.conflictingUser";
    }

    public List<Object> getMessageParams() {
        String res = duplicateUsers.size() > 10 ? StringUtils.join(duplicateUsers.toArray(new String[duplicateUsers.size()]), ",", 0, 10) + " ..." : StringUtils.join(duplicateUsers, ",");
        return Arrays.asList( (Object)duplicateUsers.size(), res);
    }
}
