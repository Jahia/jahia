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

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.*;

/**
 * 
 * User: toto
 * Date: Mar 25, 2010
 * Time: 8:35:48 PM
 * 
 */
public class JCRSortTag extends AbstractJCRTag {

    private Collection<JCRNodeWrapper> list;

    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private String properties;

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

    @Override
    public int doEndTag() throws JspException {
        List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>(list);

        String[] props = properties.split(",");
        Collections.sort(res,new NodeComparator(props));

        pageContext.setAttribute(var, res, scope);
        return super.doEndTag();
    }

    class NodeComparator implements Comparator<JCRNodeWrapper> {
        private String[] props;

        NodeComparator(String[] props) {
            this.props = props;
        }

        public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
        	boolean ignoreCase = Boolean.valueOf(props[0]).booleanValue();
        	
            for (int i = 1; i < props.length; i+=2) {
                String prop = props[i];
                String dir = props[i+1];
                int d = "desc".equals(dir) ? -1 : 1;
                String referenceProp = null;
                try {
                    prop = prop.trim();
                    if (prop.length()>0) {
                        if(prop.contains(";")) {
                            String[] split = prop.split(";");
                            prop = split[0];
                            referenceProp = split[1];
                        }
                        if (!o1.hasProperty(prop)) {
                            return -d;
                        } else if (!o2.hasProperty(prop)) {
                            return d;
                        } else {
                            Property p1 = o1.getProperty(prop);
                            Property p2 = o2.getProperty(prop);
                            if(referenceProp!=null) {
                                p1 = p1.getNode().getProperty(referenceProp);
                                p2 = p2.getNode().getProperty(referenceProp);
                            }
                            int r;
                            switch (p1.getType()) {
                                case PropertyType.DATE:
                                    r = p1.getDate().compareTo(p2.getDate());
                                    break;
                                case PropertyType.DECIMAL:
                                case PropertyType.LONG:
                                    r = Double.compare(p1.getDouble(),p2.getDouble());
                                    break;
                                default:
                                	if (ignoreCase) {
                                		r = p1.getString().compareToIgnoreCase(p2.getString());
                                	} else {
                                		r = p1.getString().compareTo(p2.getString());
                                	}
                                    break;
                            }
                            if (r != 0) {
                                return r * d;
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }

            }

            return 1;
        }

    }

}
