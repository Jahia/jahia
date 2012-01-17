/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.renderer.AbstractChoiceListRenderer;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;

import java.util.*;

/**
 * Choice list initializer, based on the resource bundle values lookup.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class ResourceBundleChoiceListInitializerImpl extends AbstractChoiceListRenderer implements ChoiceListInitializer {

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        JahiaResourceBundle rb = new JahiaResourceBundle(null, locale, getTemplatePackageName(epd));

        if (values == null || values.size() == 0) {
            List<ChoiceListValue> l = new ArrayList<ChoiceListValue>();
            String[] constr = epd.getValueConstraints();
            for (String s : constr) {
                ChoiceListValue bean = new ChoiceListValue(rb.get(epd.getResourceBundleKey() + "."
                        + JCRContentUtils.replaceColon(s), s), s);

                l.add(bean);
            }
            return l;
        } else {
            for (ChoiceListValue choiceListValue : values) {
                final String displayName = choiceListValue.getDisplayName();
                choiceListValue.setDisplayName(rb.get(epd.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(displayName),
                                                      displayName));
            }
            return values;
        }
    }

    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        return getStringRendering(context,
                (ExtendedPropertyDefinition) propertyWrapper.getDefinition(), propertyWrapper
                        .getValue().getString());
    }
    
    public String getStringRendering(Locale locale, ExtendedPropertyDefinition propDef,
            Object propertyValue) throws RepositoryException {

        String propValue = propertyValue.toString();

        JahiaResourceBundle rb = new JahiaResourceBundle(null, locale, getTemplatePackageName(propDef));

        return rb
                .get(propDef.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(propValue), propValue);
    }

    private String getTemplatePackageName(ExtendedPropertyDefinition definition) {
        String systemId = definition.getDeclaringNodeType().getSystemId();
        if(systemId.equals("system-jahia")) {
            systemId = "Default Jahia Templates";
        }
        final JahiaTemplatesPackage tpkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackage(systemId);

        return tpkg != null ? tpkg.getName() : null;
    }
}
