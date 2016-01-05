/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.render.RenderService;
import org.slf4j.Logger;

import javax.jcr.*;
import java.util.*;

/**
 * This initializer get templates depending of the type asked, if no parameter, type returns content templates (contentTemplate), in
 * page context it returns page templates (pageTemplate)
 * The query is :
 * <code>
 * "select * from [jnt:" + type + "] as n where isdescendantnode(n,['" +site.getPath()+"'])"
 * </code>
 * usage :
 * <code>
 * - j:templateNode (weakreference,choicelist[templatesNode]) mandatory < jnt:template
 * - j:templateNode (weakreference,choicelist[templatesNode=pageTemplate]) mandatory < jnt:template
 * </code>
 *
 * @author toto
 * @version 6.5
 * @since Jul 1, 2010
 */
public class TemplatesNodeChoiceListInitializer implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplatesNodeChoiceListInitializer.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        try {
            JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
            ExtendedNodeType nodetype;
            if (node == null) {
                node = (JCRNodeWrapper) context.get("contextParent");
                nodetype = (ExtendedNodeType) context.get("contextType");
            } else {
                nodetype = node.getPrimaryNodeType();
            }


            JCRNodeWrapper site = node.getResolveSite();

            final JCRSessionWrapper session = site.getSession();
            String templateType = "contentTemplate";
            if (StringUtils.isEmpty(param)) {
                if (nodetype.isNodeType("jnt:page")) {
                    templateType = "pageTemplate";
                }
            } else {
                templateType = param;
            }

            Set<String> installedModules = ((JCRSiteNode) site).getInstalledModulesWithAllDependencies();

            // get default template
            String defaultTemplate = null;
            try {
                defaultTemplate = site.hasProperty("j:defaultTemplateName") ? site.getProperty("j:defaultTemplateName").getString() : null;
            } catch (ItemNotFoundException e) {
                logger.warn("A default template has been set on site '" + site.getName() + "' but the template has been deleted");
            }
            for (String installedModule : installedModules) {
                JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(installedModule);
                if (aPackage != null) {
                    addTemplates(vs, "/modules/" + installedModule + "/" + aPackage.getVersion(), session, node, nodetype, templateType, defaultTemplate, epd, locale, context);
                }
            }

        } catch (RepositoryException e) {
            logger.error("Cannot get template", e);
        }

        Collections.sort(vs);
        return vs;
    }

    private void addTemplates(List<ChoiceListValue> vs, String path, JCRSessionWrapper session, JCRNodeWrapper node, ExtendedNodeType nodetype, String templateType, String defaultTemplate, ExtendedPropertyDefinition propertyDefinition, Locale locale, Map<String, Object> context) throws RepositoryException {
        List<JCRNodeWrapper> nodes = RenderService.getInstance().getTemplateNodes(null, path, "jnt:"+templateType, false, session);
        for (JCRNodeWrapper templateNode : nodes) {
            boolean ok = true;
            if (templateNode.hasProperty("j:applyOn")) {
                ok = false;
                Value[] types = templateNode.getProperty("j:applyOn").getValues();
                for (Value value : types) {
                    if (nodetype.isNodeType(value.getString())) {
                        ok = true;
                        break;
                    }
                }
                if (types.length == 0) {
                    ok = true;
                }
            }
            if (ok && templateNode.hasProperty("j:hiddenTemplate")) {
                ok = !templateNode.getProperty("j:hiddenTemplate").getBoolean();
            }
            if ("pageTemplate".equals(templateType)) {
                ok &= node.getResolveSite().hasPermission("template-" + templateNode.getName());
            }

            if (!ok) {
                // check the current value of the page template, if it's the current template node, we will have
                // to let it pass anyway.
                if (context.get("contextNode") != null && node.hasProperty("j:templateName")) {
                    try {
                        if (node.getProperty("j:templateName").getString() != null &&
                                node.getProperty("j:templateName").getString().equals(templateNode.getName())) {
                            ok = true;
                        }
                    } catch (ItemNotFoundException infe) {
                        // if we don't have access to the template not we simply don't do allow the template
                        ok = false;
                    }
                }
            }

            if (ok) {
                String templateName = templateNode.getName();
                try {
                    Property templateTitleProperty = templateNode.getI18N(locale).getProperty(Constants.JCR_TITLE);
                    if (templateTitleProperty != null) {
                        String templateTitle = templateTitleProperty.getString();
                        if (StringUtils.isNotEmpty(templateTitle)) {
                            templateName = templateTitle;
                        }
                    }
                } catch (RepositoryException re) {
                    logger.debug("No title for template "+templateNode.getPath()+" in locale " + locale + ", will use template system name as display name");
                }
                ChoiceListValue v;
                if (propertyDefinition.getRequiredType() == PropertyType.STRING) {
                    v = new ChoiceListValue(templateName, null, session.getValueFactory().createValue(templateNode.getName(), PropertyType.STRING));
                } else {
                    v = new ChoiceListValue(templateName, null, session.getValueFactory().createValue(templateNode.getIdentifier(), PropertyType.WEAKREFERENCE));
                }
                if (StringUtils.equals(templateNode.getName(), defaultTemplate)) {
                    v.addProperty("defaultProperty", true);
                }
                vs.add(v);
            }
        }
    }
}
