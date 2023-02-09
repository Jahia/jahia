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

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.renderer.AbstractChoiceListRenderer;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;

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
        JahiaTemplatesPackage pkg = epd.getDeclaringNodeType().getTemplatePackage();
        java.util.ResourceBundle rb = ResourceBundles.get(pkg != null ? pkg : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT), locale);

        if (values == null || values.size() == 0) {
            List<ChoiceListValue> l = new ArrayList<ChoiceListValue>();
            String[] constr = epd.getValueConstraints();
            for (String s : constr) {
                ChoiceListValue bean = new ChoiceListValue(Messages.get(rb, epd.getResourceBundleKey() + "."
                        + JCRContentUtils.replaceColon("".equals(s.trim())?"empty":s), "".equals(s.trim())?"empty":s), s);

                l.add(bean);
            }
            return l;
        } else {
            for (ChoiceListValue choiceListValue : values) {
                final String displayName = choiceListValue.getDisplayName();
                choiceListValue.setDisplayName(Messages.get(rb, epd.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(displayName),
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

        JahiaTemplatesPackage pkg = propDef.getDeclaringNodeType().getTemplatePackage();
        return Messages.get(pkg != null ? pkg : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT),
                propDef.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(propValue), locale, propValue);
    }
}
