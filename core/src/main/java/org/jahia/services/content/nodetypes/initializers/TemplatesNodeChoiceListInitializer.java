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

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
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

            List<String> installedModules = ((JCRSiteNode) site).getInstalledModules();
            for (int i = 0; i < installedModules.size(); i++) {
                String installedModule = installedModules.get(i);
                JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(installedModule);
                if (aPackage != null) {
                    for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                        if (!installedModules.contains(depend.getRootFolder())) {
                            installedModules.add(depend.getRootFolder());
                        }
                    }
                } else {
                    logger.error("Couldn't find module directory for module '" + installedModule + "' installed in site '" + site.getPath() + "'");
                }
            }

            // get default template
            JCRNodeWrapper defaultTemplate = null;
            try {
                defaultTemplate = site.hasProperty("j:defaultTemplate") ? (JCRNodeWrapper) site.getProperty("j:defaultTemplate").getNode() : null;
            } catch (ItemNotFoundException e) {
                logger.warn("A default template has been set on site '" + site.getName() + "' but the template has been deleted");
            }
            for (String installedModule : installedModules) {
                JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(installedModule);
                if (aPackage != null) {
                    addTemplates(vs, "/modules/" + installedModule + "/" + aPackage.getVersion(), session, node, nodetype, templateType, defaultTemplate, epd, locale);
                }
            }

        } catch (RepositoryException e) {
            logger.error("Cannot get template", e);
        }

        Collections.sort(vs);
        return vs;
    }

    private void addTemplates(List<ChoiceListValue> vs, String path, JCRSessionWrapper session, JCRNodeWrapper node, ExtendedNodeType nodetype, String templateType, JCRNodeWrapper defaultTemplate, ExtendedPropertyDefinition propertyDefinition, Locale locale) throws RepositoryException {
        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        QueryResult result = queryManager.createQuery(
                "select * from [jnt:" + templateType + "] as n where isdescendantnode(n,['" + path + "'])", Query.JCR_SQL2).execute();
        final NodeIterator iterator = result.getNodes();
        while (iterator.hasNext()) {
            JCRNodeWrapper templateNode = (JCRNodeWrapper) iterator.next();

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
                ok &= node.hasPermission("template-" + templateNode.getName());
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
<<<<<<< .working
=======
                    } catch (RepositoryException re) {
                        logger.debug("No title for template "+templateNode.getPath()+" in locale " + locale + ", will use template system name as display name");
>>>>>>> .merge-right.r45057
                    }
                } catch (RepositoryException re) {
                    logger.warn("No title for template "+templateNode.getPath()+" in locale " + locale + ", will use template system name as display name");
                }
                ChoiceListValue v;
                if (propertyDefinition.getRequiredType() == PropertyType.STRING) {
                    v = new ChoiceListValue(templateName, null, session.getValueFactory().createValue(templateNode.getName(), PropertyType.STRING));
                } else {
                    v = new ChoiceListValue(templateName, null, session.getValueFactory().createValue(templateNode.getIdentifier(), PropertyType.WEAKREFERENCE));
                }
                if (defaultTemplate != null && templateNode.getPath().equals(defaultTemplate.getPath())) {
                    v.addProperty("defaultProperty", true);
                }
                vs.add(v);
            }
        }
    }
}
