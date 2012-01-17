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
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Choice list initializer that looks up child nodes of the specified one
 * filtering them out by the specified type, if any is provided.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
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
        if (param != null) {
            String[] s = param.split(";");
            String nodetype = null;
            if (s.length > 1) {
                nodetype = s[1];
            }
            try {
                JCRSiteNode site = null;
                JCRNodeWrapper contextNode = (JCRNodeWrapper) context.get("contextParent");
                if (contextNode == null) {
                    contextNode = (JCRNodeWrapper) context.get("contextNode");
                }
                if (contextNode != null) {
                    site = contextNode.getResolveSite();
                } else {
                    final JahiaSite defaultSite = JahiaSitesBaseService.getInstance().getDefaultSite();
                    if (defaultSite != null) {
                        site = (JCRSiteNode) sessionFactory.getCurrentUserSession().getNode("/sites/"+ defaultSite.getSiteKey());
                    } else {
                        site = (JCRSiteNode) sessionFactory.getCurrentUserSession().getNode(JCRContentUtils.getSystemSitePath());
                    }
                }
                String path = s[0];
                String returnType = "";
                if (s.length > 2) {
                    returnType = s[2];
                }
                Locale fallbackLocale = null;
                if (site != null) {
                    fallbackLocale = site.isMixLanguagesActive() ? LanguageCodeConverters.languageCodeToLocale(
                            site.getDefaultLanguage()) : null;
                    path = path.replace("$currentSite", site.getPath());
                }
                boolean subTree = false;
                if (path.endsWith("//*")) {
                    path = StringUtils.substringBeforeLast(path, "//*");
                    subTree = true;
                }
                final JCRSessionWrapper jcrSessionWrapper = sessionFactory.getCurrentUserSession(null, locale, fallbackLocale);
                final JCRNodeWrapper node = jcrSessionWrapper.getNode(path);
                addSubnodes(listValues, nodetype, node, subTree, returnType);
            } catch (PathNotFoundException e) {
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
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
                listValues.add(new ChoiceListValue(displayName, 
                        "name".equals(returnType)?nodeWrapper.getName():nodeWrapper.getIdentifier()));
            }
            if (subTree) {
                addSubnodes(listValues, nodetype, nodeWrapper, subTree, returnType);
            }
        }
    }
}
