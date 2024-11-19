/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRFileNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.render.RenderService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.query.Query;
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
        List<ChoiceListValue> tmpVs = new ArrayList<ChoiceListValue>();
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
                    addTemplates(tmpVs, "/modules/" + installedModule + "/" + aPackage.getVersion(), session, node, nodetype, templateType, defaultTemplate, epd, locale, context);
                }
            }

            // test on create / edit engine, display page models only in create engine
            // contextNode is null in create engine
            if (context.get("contextNode") == null) {
                // Add page Models
                Query queryPageModels = session.getWorkspace().getQueryManager().createQuery("select * from [jmix:canBeUseAsTemplateModel] as tpl " +
                        "where isdescendantnode(tpl,['" + site.getPath() + "'])", Query.JCR_SQL2);
                QueryResult qrPageModels = queryPageModels.execute();
                NodeIterator niPageModels = qrPageModels.getNodes();

                // filter out parent nodes
                List<Node> templateNodes = new ArrayList<>();
                while (niPageModels.hasNext()) {
                    Node n = niPageModels.nextNode();
                    if (!node.getPath().startsWith(n.getPath()) && !n.isNodeType("jmix:markedForDeletion")) {
                        templateNodes.add(n);
                    }
                }

                if (templateNodes.size() > 0) {
                    vs.add(new ChoiceListValue(Messages.getInternal("org.jahia.services.content.nodetypes.initializers.templates.title", locale), ""));
                    Collections.sort(tmpVs);
                    vs.addAll(tmpVs);
                    vs.add(new ChoiceListValue(Messages.getInternal("org.jahia.services.content.nodetypes.initializers.pageModels.title", locale), ""));
                    tmpVs.clear();

                    for (Node templateNode : templateNodes) {
                        String title;
                        if (templateNode.hasProperty("j:pageTemplateTitle")) {
                            title = templateNode.getProperty("j:pageTemplateTitle").getString();
                        } else {
                            title = templateNode.getName();
                        }
                        ChoiceListValue templateModelValue = new ChoiceListValue(" " + title, templateNode.getPath());
                        templateModelValue.addProperty("addMixin", "jmix:createdFromPageModel");
                        tmpVs.add(templateModelValue);
                    }
                }
            }
            Collections.sort(tmpVs);
            vs.addAll(tmpVs);
        } catch (RepositoryException e) {
            logger.error("Cannot get template", e);
        }

        return vs;
    }

    private void addTemplates(List<ChoiceListValue> vs, String path, JCRSessionWrapper session, JCRNodeWrapper node, ExtendedNodeType nodetype, String templateType, String defaultTemplate, ExtendedPropertyDefinition propertyDefinition, Locale locale, Map<String, Object> context) throws RepositoryException {
        List<JCRNodeWrapper> nodes = RenderService.getInstance().getTemplateNodes(null, path, "jnt:" + templateType, false, session);
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
                    if (templateNode.isNodeType(Constants.JAHIAMIX_RB_TITLE)) {
                        templateName = templateNode.getDisplayableName();
                    } else {
                        Property templateTitleProperty = templateNode.getI18N(locale).getProperty(Constants.JCR_TITLE);
                        if (templateTitleProperty != null) {
                            String templateTitle = templateTitleProperty.getString();
                            if (StringUtils.isNotEmpty(templateTitle)) {
                                templateName = templateTitle;
                            }
                        }
                    }
                } catch (RepositoryException re) {
                    logger.debug("No title for template {} in locale {}, will use template system name as display name", templateNode.getPath(), locale);
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

                if (templateNode.isNodeType("jnt:pageTemplate") && templateNode.hasProperty("j:templateThumbnail")) {
                    v.addProperty("image", ((JCRFileNode) templateNode.getProperty("j:templateThumbnail").getNode()).getUrl());
                }

                vs.add(v);
            }
        }
    }
}
