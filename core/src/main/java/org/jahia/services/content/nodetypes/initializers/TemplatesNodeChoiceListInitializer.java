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
 *     "select * from [jnt:" + type + "] as n where isdescendantnode(n,['" +site.getPath()+"'])"
 * </code>
 * usage :
 * <code>
 *     - j:templateNode (weakreference,choicelist[templatesNode]) mandatory < jnt:template
 *     - j:templateNode (weakreference,choicelist[templatesNode=pageTemplate]) mandatory < jnt:template
 * </code>
 * @version 6.5
 * @author toto
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
            final QueryManager queryManager = session.getWorkspace().getQueryManager();
            String type = "contentTemplate";
            if (StringUtils.isEmpty(param)) {
                if (nodetype.isNodeType("jnt:page")) {
                    type = "pageTemplate";
                }
            } else {
                type = param;
            }

            QueryResult result = queryManager.createQuery(
                    "select * from [jnt:" + type + "] as n where isdescendantnode(n,['" +site.getPath()+"'])", Query.JCR_SQL2).execute();
            // get default template
            JCRNodeWrapper  defaultTemplate = null;
            try {
                defaultTemplate = site.hasProperty("j:defaultTemplate")? (JCRNodeWrapper) site.getProperty("j:defaultTemplate").getNode():null;
            } catch (ItemNotFoundException e) {
                logger.warn("A default template has been set on site '" + site.getName() + "' but the template has been deleted");
            }
                final NodeIterator iterator = result.getNodes();
            while (iterator.hasNext()) {
                JCRNodeWrapper templateNode = (JCRNodeWrapper) iterator.next();

                boolean ok = true;
                if (templateNode. hasProperty("j:applyOn")) {
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
                if ("pageTemplate".equals(type)) {
                    ok &= node.hasPermission("template-"+templateNode.getName());
                }

                if (ok) {
                    ChoiceListValue v = new ChoiceListValue(templateNode.getName(), null, session.getValueFactory().createValue(templateNode.getIdentifier(),
                            PropertyType.WEAKREFERENCE));
                    if (defaultTemplate !=  null && templateNode.getPath().equals(defaultTemplate.getPath())) {
                        v.addProperty("defaultProperty",true);
                    }
                    vs.add(v);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get template",e);
        }

        Collections.sort(vs);
        return vs;
    }
}
