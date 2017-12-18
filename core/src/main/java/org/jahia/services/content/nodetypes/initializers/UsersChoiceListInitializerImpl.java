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

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.QueryManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.PropertyType;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Choice list initializer to provide a selection of all users
 *
 * @author : toto
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class UsersChoiceListInitializerImpl implements ChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(UsersChoiceListInitializerImpl.class);
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();

        try {
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            Query q = qm.createQuery("select * from [jnt:user] as user order by user.name", Query.JCR_SQL2);
            QueryResult qr = q.execute();
            NodeIterator ni = qr.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
                if (node.getName().equals("guest")) {
                    continue;
                }

                String name = "";
                if (node.hasProperty("j:firstName")) {
                    name += node.getProperty("j:firstName").getString() + " ";
                }
                if (node.hasProperty("j:lastName")) {
                    name += node.getProperty("j:lastName").getString();
                }
                name = name.trim();
                if (name.equals("")) {
                    name = node.getName();
                }
                if (epd.getRequiredType() == PropertyType.STRING) {
                    vs.add(new ChoiceListValue(name, new HashMap<String,Object>(), new ValueImpl(node.getPath(), epd.getRequiredType(), false)));
                } else {
                    vs.add(new ChoiceListValue(name, new HashMap<String,Object>(), new ValueImpl(node.getIdentifier(), epd.getRequiredType(), false)));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return vs;
    }
}
