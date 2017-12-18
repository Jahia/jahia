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

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;

/**
 * User: david
 * Date: Dec 7, 2010
 * Time: 9:49:38 AM
 */
public class MenusChoiceListInitializerImpl implements ChoiceListInitializer{
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MenusChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        Set<ChoiceListValue> set = new  TreeSet<ChoiceListValue>();
        try {
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
            if (node == null) {
                node = (JCRNodeWrapper) context.get("contextParent");
            }
            JCRSiteNode site = node.getResolveSite();

            final JahiaTemplateManagerService service = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

            scanForNavMenus(site.getPath(), set, qm);

            for (String s : site.getAllInstalledModules()) {
                JahiaTemplatesPackage module = service.getTemplatePackageById(s);
                if (module != null) {
                    final String modulePath = "/modules/" + JCRContentUtils.sqlEncode(module.getIdWithVersion());
                    scanForNavMenus(modulePath, set, qm);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        listValues.addAll(set);
        return listValues;

    }

    private void scanForNavMenus(String rootPath, Set<ChoiceListValue> set, QueryManager qm) throws RepositoryException {
        QueryResult result = qm.createQuery(
                "select * from [jmix:navMenuComponent] as n where isdescendantnode(n,['" + rootPath + "'])", Query.JCR_SQL2).execute();
        final NodeIterator resultNodes = result.getNodes();
        while (resultNodes.hasNext()) {
            JCRNodeWrapperImpl nodeWrapper = (JCRNodeWrapperImpl) resultNodes.nextNode();
            String displayName = nodeWrapper.getDisplayableName();
            set.add(new ChoiceListValue(displayName, nodeWrapper.getName()));
        }
    }
}
