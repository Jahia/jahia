/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import java.util.List;
import java.util.Locale;

import javax.jcr.Value;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default value initializer for a node property that looks up the value from a resource bundle.
 *
 * @author Sergiy Shyrkov
 */
public class ResourceBundle implements I15dValueInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ResourceBundle.class);

    @Override
    public Value[] getValues(ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        return getValues(declaringPropertyDefinition, params, null);
    }

    @Override
    public Value[] getValues(ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params, Locale locale) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException(
                    "This value initializer expects at least the label key to be provided as a parameter");
        }

        if (locale == null) {
            // no locale passed -> use default one that is configured
            locale = SettingsBean.getInstance().getDefaultLocale();
        }

        String label = null;

        String key = params.get(0);
        if (params.size() > 1) {
            // resource bundle specified explicitly -> use it
            label = Messages.get(params.get(1), key, locale, key);
        } else {
            // no resource bundle specified -> detect the declaring mode for the node type and use its resource bundle
            JahiaTemplatesPackage pkg = declaringPropertyDefinition.getDeclaringNodeType().getTemplatePackage();
            if (pkg == null) {
                pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT);
            }
            if (pkg == null) {
                label = DEFAULT_VALUE;
            } else {
                label = Messages.get(pkg, key, locale, key);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Computed default value for property [{}].[{}] and locale {}: {}", new String[] {
                    declaringPropertyDefinition.getDeclaringNodeType().getName(),
                    declaringPropertyDefinition.getName(), String.valueOf(locale), label });
        }

        return new Value[] { new ValueImpl(label) };
    }

}
