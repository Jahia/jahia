package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.xml.sax.Attributes;

import java.util.Set;
import java.util.TreeSet;

/**
 * Validate that user in import does not already exist
 *
 */
public class UserValidator implements ImportValidator {

    private Set<String> duplicateUsers = new TreeSet<String>();

    private JahiaUserManagerService userManagerService;

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    @Override
    public ValidationResult getResult() {
        return new UserValidatorResult(duplicateUsers);
    }

    @Override
    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {
        String type = atts.getValue("jcr:primaryType");
        if (type != null && type.equals("jnt:user")) {
            String site = null;
            currentPath = StringUtils.substringAfter(currentPath, "/content");
            if (currentPath.startsWith("/sites/")) {
                String[] split = currentPath.split("/");
                if (split.length > 2) {
                    site = split[2];
                }
            }
            JCRUserNode userNode = userManagerService.lookupUser(decodedLocalName, site);
            if (userNode != null && !userNode.getPath().equals(currentPath)) {
                duplicateUsers.add(decodedLocalName);
            }
        }
    }
}
