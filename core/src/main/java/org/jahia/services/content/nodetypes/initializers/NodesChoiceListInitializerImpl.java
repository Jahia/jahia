/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Choice list initializer that looks up child nodes of the specified one 
 * filtering them out by the specified type, if any is provided. 
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class NodesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(NodesChoiceListInitializerImpl.class);
    private JCRSessionFactory sessionFactory;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<ChoiceListValue> getChoiceListValues(ProcessingContext context, ExtendedPropertyDefinition epd,
                                                     ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        if (param != null) {
            String[] s = param.split(";");
            String nodetype = null;
            if (s.length > 1) {
                nodetype = s[1];
            }
            try {
                final JCRSessionWrapper jcrSessionWrapper = sessionFactory.getCurrentUserSession(null,
                                                                                                 context.getLocale());
                final JCRNodeWrapper node = jcrSessionWrapper.getNode(s[0].replace("$currentSite",context.getSiteKey()));
                final NodeIterator nodeIterator = node.getNodes();
                while (nodeIterator.hasNext()) {
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
                    if (nodeWrapper.isNodeType(nodetype)) {
                        String displayName = nodeWrapper.getName();
                        if (nodeWrapper.hasProperty("jcr:title")) {
                            displayName = nodeWrapper.getProperty("jcr:title").getString();
                        }
                        listValues.add(new ChoiceListValue(displayName, new HashMap<String, Object>(), new ValueImpl(
                                nodeWrapper.getIdentifier(), PropertyType.STRING, false)));
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return listValues;
    }
}
