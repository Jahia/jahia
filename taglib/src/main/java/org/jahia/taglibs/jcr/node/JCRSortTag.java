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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 25, 2010
 * Time: 8:35:48 PM
 * To change this template use File | Settings | File Templates.
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
            for (int i = 0; i < props.length; i+=2) {
                String prop = props[i];
                String dir = props[i+1];
                int d = "desc".equals(dir) ? -1 : 1;
                try {
                    prop = prop.trim();
                    if (prop.length()>0) {
                        if (!o1.hasProperty(prop)) {
                            return -d;
                        } else if (!o2.hasProperty(prop)) {
                            return d;
                        } else {
                            Property p1 = o1.getProperty(prop);
                            Property p2 = o2.getProperty(prop);
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
                                    r = p1.getString().compareTo(p2.getString());
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
