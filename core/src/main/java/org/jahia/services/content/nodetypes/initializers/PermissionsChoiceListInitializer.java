/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
