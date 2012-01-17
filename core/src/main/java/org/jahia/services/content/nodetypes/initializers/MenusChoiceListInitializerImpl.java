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

import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
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
        String nodetype = "jnt:navMenu";
        try {
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
            if (node == null) {
                node = (JCRNodeWrapper) context.get("contextParent");
            }
            JCRNodeWrapper site = node.getResolveSite();

            QueryResult result = qm.createQuery(
                    "select * from [" + nodetype + "] as n where isdescendantnode(n,['" +site.getPath()+"'])", Query.JCR_SQL2).execute();
            final NodeIterator ni = result.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapperImpl nodeWrapper = (JCRNodeWrapperImpl) ni.nextNode();
                String displayName = nodeWrapper.getDisplayableName();
                set.add(new ChoiceListValue(displayName, nodeWrapper.getIdentifier()));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        listValues.addAll(set);
        return listValues;

    }
}
