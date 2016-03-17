package org.jahia.data.viewhelper.principal;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePrincipalViewHelperExtensionImpl implements PrincipalViewHelperExtension {

    private static final Logger logger = LoggerFactory.getLogger(SimplePrincipalViewHelperExtensionImpl.class);

    @Override
    public String getUserDisplayName(JahiaUser jahiaUser) {
        StringBuilder name = new StringBuilder();
        String value = jahiaUser.getProperty("j:firstName");
        if (StringUtils.isNotEmpty(value)) {
            name.append(value);
        }
        value = jahiaUser.getProperty("j:lastName");
        if (StringUtils.isNotEmpty(value)) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(value);
        }

        if (name.length() == 0) name.append(jahiaUser.getUsername());
        else name.append(" (").append(jahiaUser.getUsername()).append(")");

        return name.toString();
    }
}
