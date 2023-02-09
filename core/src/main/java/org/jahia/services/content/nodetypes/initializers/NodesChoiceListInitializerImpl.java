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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Choice list initializer that looks up child nodes of the specified one
 * filtering them out by the specified type, if any is provided.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 17 nov. 2009
 */
public class NodesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(NodesChoiceListInitializerImpl.class);
    private JCRSessionFactory sessionFactory;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
            Map<String, Object> context) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        if (CollectionUtils.isNotEmpty(values)) listValues.addAll(values);
        if (param != null) {
            for (String subParam : Patterns.PIPE.split(param)) {
                String[] s = Patterns.SEMICOLON.split(subParam);
                String nodetype = null;
                if (s.length > 1) {
                    nodetype = s[1];
                }
                try {
                    JCRSiteNode site;
                    JCRNodeWrapper resolvedNode = (JCRNodeWrapper) context.get("contextParent");
                    JCRNodeWrapper ctxNode = (JCRNodeWrapper) context.get("contextNode");
                    if (resolvedNode == null) {
                        resolvedNode = ctxNode;
                    }

                    // Always try to resolve site based on passed context node if it is available
                    if (ctxNode != null) {
                        site = ctxNode.getResolveSite();
                    } else if (resolvedNode != null) {
                        site = resolvedNode.getResolveSite();
                    } else {
                        final JahiaSite defaultSite = JahiaSitesService.getInstance().getDefaultSite();
                        if (defaultSite != null) {
                            site = (JCRSiteNode) sessionFactory.getCurrentUserSession().getNode("/sites/" + defaultSite.getSiteKey());
                        } else {
                            site = (JCRSiteNode) sessionFactory.getCurrentUserSession().getNode(JCRContentUtils.getSystemSitePath());
                        }
                        resolvedNode = site;
                    }
                    String path = s[0];
                    String returnType = "";
                    if (s.length > 2) {
                        returnType = s[2];
                    }
                    path = StringUtils.replace(path,"$currentSiteTemplatesSet","/modules/" + site.getTemplatePackage().getIdWithVersion());
                    if (StringUtils.contains(path,"$currentSite/templates/")) {
                        path = StringUtils.replace(path,"$currentSite", "/modules/" + site.getTemplatePackage().getIdWithVersion());
                    } else {
                        path = StringUtils.replace(path,"$currentSite", site.getPath());
                    }
                    boolean subTree = false;
                    if (path.endsWith("//*")) {
                        path = StringUtils.substringBeforeLast(path, "//*");
                        subTree = true;
                    }
                    JCRSessionWrapper jcrSessionWrapper = resolvedNode.getSession();
                    JCRNodeWrapper node;
                    if (path.equals(".")) {
                        node = resolvedNode;
                    } else if (path.startsWith("./")) {
                        node = resolvedNode.getNode(path.substring(2));
                    } else {
                        node = jcrSessionWrapper.getNode(path);
                    }
                    addSubnodes(listValues, nodetype, node, subTree, returnType, jcrSessionWrapper);
                } catch (PathNotFoundException e) {
                    logger.debug("Cannot find node " + e.getMessage(), e);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return listValues;
    }

    private void addSubnodes(ArrayList<ChoiceListValue> listValues, String nodeType, JCRNodeWrapper node, boolean subTree, String returnType, JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
        final String queryStmt;

        if (subTree) {
            queryStmt = "select * from [" + JCRContentUtils.sqlEncode(nodeType) + "] as t where isdescendantnode(t, ['" + JCRContentUtils.sqlEncode(node.getPath()) + "']) ORDER BY t.[j:nodename] ASC";
        } else {
            queryStmt = "select * from [" + JCRContentUtils.sqlEncode(nodeType) + "] as t where ischildnode(t, ['" + JCRContentUtils.sqlEncode(node.getPath()) + "']) ORDER BY t.[j:nodename] ASC";
        }
        final Query query = jcrSessionWrapper.getWorkspace().getQueryManager().createQuery(queryStmt, Query.JCR_SQL2);
        final NodeIterator nodeIterator = query.execute().getNodes();
        while (nodeIterator.hasNext()) {
            final JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            final String displayName = nodeWrapper.getDisplayableName();
            listValues.add(new ChoiceListValue(displayName, "name".equals(returnType) ? nodeWrapper.getName() : nodeWrapper.getIdentifier()));
        }
    }
}
