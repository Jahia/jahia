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

package org.jahia.taglibs.jcr.node;

import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Mar 25, 2010
 * Time: 8:35:48 PM
 * 
 */
public class JCRFilterTag extends AbstractJCRTag {
    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(JCRFilterTag.class);
    private Collection<JCRNodeWrapper> list;

    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private String properties;
    private JCRNodeWrapper node;

    public void setList(Object o) {
        if (o instanceof Collection) {
            this.list = (Collection) o;
        } else if (o instanceof Iterator) {
            this.list = new ArrayList<JCRNodeWrapper>();
            final Iterator iterator = (Iterator) o;
            while (iterator.hasNext()) {
                JCRNodeWrapper e = (JCRNodeWrapper) iterator.next();
                this.list.add(e);
            }
        }
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    @Override
    public int doEndTag() throws JspException {
        List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>();
        try {
            final JSONObject jsonObject = new JSONObject(properties);
            final String uuid = (String) jsonObject.get("uuid");
            if (uuid.equals(node.getIdentifier())) {
                String name = (String) jsonObject.get("name");
                String value = (String) jsonObject.get("value");
                String op = (String) jsonObject.get("op");
                String type = null;
                String format;
                SimpleDateFormat dateFormat = null;
                try {
                    type = (String) jsonObject.get("type");
                    format = (String) jsonObject.get("format");
                    dateFormat = new SimpleDateFormat(format);
                } catch (JSONException e) {
                }
                for (JCRNodeWrapper re : list) {
                    final JCRPropertyWrapper property = re.getProperty(name);
                    if (property != null) {
                        if ("eq".equals(op)) {
                            if(type!=null&&"date".equals(type)) {
                                if(dateFormat!=null && dateFormat.format(property.getDate().getTime()).equals(value)) {
                                    res.add(re);
                                }
                            }
                        }
                    }
                }
                pageContext.setAttribute(var, res, scope);
            } else {
                pageContext.setAttribute(var, list, scope);
            }
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return super.doEndTag();
    }

}