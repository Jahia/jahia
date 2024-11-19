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
