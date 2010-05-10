package org.jahia.taglibs.jcr.node;

import org.apache.log4j.Logger;
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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 25, 2010
 * Time: 8:35:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRFilterTag extends AbstractJCRTag {
    private static transient Logger logger = Logger.getLogger(JCRFilterTag.class);
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