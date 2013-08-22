package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.security.Privilege;
import java.util.*;

public class PermissionsChoiceListInitializer implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(PermissionsChoiceListInitializer.class);

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> result = new ArrayList<ChoiceListValue>();
        Privilege[] p = JahiaPrivilegeRegistry.getRegisteredPrivileges();
        try {
            for (Privilege privilege : p) {
                final String jcrName = JCRContentUtils.getJCRName(privilege.getName(), JCRSessionFactory.getInstance().getNamespaceRegistry());
                String localName = jcrName;
                if (localName.contains(":")) {
                    localName = StringUtils.substringAfter(localName, ":");
                }
                String title = StringUtils.capitalize(localName.replaceAll("([A-Z])", " $0").replaceAll("[_-]", " ").toLowerCase());
                final String rbName = localName.replaceAll("-", "_");
                result.add(new ChoiceListValue(Messages.getInternal("label.permission." + rbName, locale, title), jcrName));
            }
        } catch (RepositoryException e) {
            logger.error("Repository exception", e);
        }
        Collections.sort(result);
        return result;
    }
}
