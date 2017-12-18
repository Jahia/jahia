/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
                    JCRNodeWrapper contextNode = (JCRNodeWrapper) context.get("contextParent");
                    if (contextNode == null) {
                        contextNode = (JCRNodeWrapper) context.get("contextNode");
                    }
                    if (contextNode != null) {
                        site = contextNode.getResolveSite();
                    } else {
                        final JahiaSite defaultSite = JahiaSitesService.getInstance().getDefaultSite();
                        if (defaultSite != null) {
                            site = (JCRSiteNode) sessionFactory.getCurrentUserSession().getNode("/sites/" + defaultSite.getSiteKey());
                        } else {
                            site = (JCRSiteNode) sessionFactory.getCurrentUserSession().getNode(JCRContentUtils.getSystemSitePath());
                        }
                        contextNode = site;
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
                    JCRSessionWrapper jcrSessionWrapper = contextNode.getSession();
                    JCRNodeWrapper node;
                    if (path.equals(".")) {
                        node = contextNode;
                    } else if (path.startsWith("./")) {
                        node = contextNode.getNode(path.substring(2));
                    } else {
                        node = jcrSessionWrapper.getNode(path);
                    }
                    addSubnodes(listValues, nodetype, node, subTree, returnType);
                } catch (PathNotFoundException e) {
                    logger.debug("Cannot find node " + e.getMessage(), e);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return listValues;
    }

    private void addSubnodes(ArrayList<ChoiceListValue> listValues, String nodetype, JCRNodeWrapper node, boolean subTree, String returnType) throws RepositoryException {
        final NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            if (nodeWrapper.isNodeType(nodetype)) {
                String displayName = nodeWrapper.getDisplayableName();
                listValues.add(new ChoiceListValue(displayName, "name".equals(returnType) ? nodeWrapper.getName() : nodeWrapper.getIdentifier()));
            }
            if (subTree) {
                addSubnodes(listValues, nodetype, nodeWrapper, subTree, returnType);
            }
        }
    }
}
