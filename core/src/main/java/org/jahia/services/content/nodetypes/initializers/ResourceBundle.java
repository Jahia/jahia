/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
                pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName("default");
            }
            if (pkg == null) {
                label = key;
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
