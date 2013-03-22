package org.jahia.modules.serversettings.users.admin;

import org.jahia.modules.serversettings.users.management.UserProperties;
import org.springframework.binding.validation.ValidationContext;

/**
 * Bean to handle Admin properties flow.
 * 
 * @author david
 */
public class AdminProperties extends UserProperties {
    private static final long serialVersionUID = -6704900404057859326L;

    public AdminProperties() {
        super();
    }

    public AdminProperties(String userKey) {
        super(userKey);
    }

    /**
     * Perform a validation on email and password
     * 
     * @param context
     */
    public void validateAdminPropertiesForm(ValidationContext context) {
        validateEditUser(context);
    }

    /**
     * Perform a validation for the new site administrator user
     * 
     * @param context
     */
    public void validateSiteAdminPropertiesForm(ValidationContext context) {
        validateCreateUser(context);
    }
}
