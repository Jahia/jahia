/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.jcr.node;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: toto Date: Mar 25, 2010 Time: 8:35:48 PM
 */
public class JCRFilterTag extends AbstractJCRTag {
    private static final long serialVersionUID = 7977579361895318499L;
    public static final String EQ = "eq";
    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(JCRFilterTag.class);
    private Collection<JCRNodeWrapper> list;

    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private String properties;
    private String types;
    private JCRNodeWrapper node;

    public void setList(Object o) {
        if (o instanceof Collection) {
            this.list = new ArrayList<JCRNodeWrapper>((Collection<? extends JCRNodeWrapper>) o);
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

    public void setTypes(String types) {
        this.types = types;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            Collection<JCRNodeWrapper> res = null;

            // filter based on properties
            if (StringUtils.isNotEmpty(properties) && node != null) {
                final JSONObject jsonObject = new JSONObject(properties);
                final String uuid = (String) jsonObject.get("uuid");
                if (uuid.equals(node.getIdentifier())) {
                    res = new ArrayList<>();

                    final String name = (String) jsonObject.get("name");
                    final String valueAsString = (String) jsonObject.get("value");

                    String op = getValueOrDefaultIfMissingOrEmpty(jsonObject, "op", EQ);

                    // check for negation operator
                    boolean isNegated = false;
                    if (op.startsWith("!")) {
                        isNegated = true; // we want the negated usual outcome
                        op = op.substring(1); // remove negation operator
                    }

                    // if we don't have a type, assume "String"
                    String type = getValueOrDefaultIfMissingOrEmpty(jsonObject, "type", "String");

                    // optional format
                    SimpleDateFormat dateFormat = null;
                    String format = getValueOrDefaultIfMissingOrEmpty(jsonObject, "format", null);
                    if (format != null) {
                        dateFormat = new SimpleDateFormat(format);
                    }

                    // backward compatibility with previously documented "date" type where appropriate JCR type should be "Date"
                    final boolean isLowerCaseDate = "date".equals(type);
                    Value value = isLowerCaseDate ? null : JCRValueFactoryImpl.getInstance().createValue(valueAsString, PropertyType.valueFromName(type));

                    for (JCRNodeWrapper re : list) {
                        if (re.hasProperty(name)) {
                            final JCRPropertyWrapper property = re.getProperty(name);
                            if (property != null) {
                                if (EQ.equals(op)) {

                                    // backward compatibility
                                    if (isLowerCaseDate) {
                                        if (dateFormat != null && dateFormat.format(property.getDate().getTime()).equals(valueAsString)) {
                                            res.add(re);
                                        }
                                    } else if (property.isMultiple()) {
                                        final JCRValueWrapper[] values = property.getValues();
                                        for (Value wrappedValue : values) {
                                            if (wrappedValue.equals(value)) {
                                                res.add(re);
                                            }
                                        }

                                    } else if (property.getValue().equals(value)) {
                                        res.add(re);
                                    }
                                }
                            }
                        }
                    }

                    // if we had negated the operation, remove all matching elements from the original list
                    if (isNegated) {
                        list.removeAll(res);
                        res = list;
                    }
                }
            }

            // filter based on node types
            if (StringUtils.isNotEmpty(types)) {
                if (res != null) {
                    list = new ArrayList<>(res);
                }
                res = new ArrayList<>();
                List<String> filteredTypes = Arrays.asList(types.split("\\s*,\\s*"));
                for (JCRNodeWrapper re : list) {
                    if(re.isNodeType("jmix:skipConstraintCheck")) {
                        res.add(re);
                        continue;
                    }
                    if (JCRContentUtils.isNodeType(re, filteredTypes)) {
                        res.add(re);
                    } else if(re.isNodeType("jnt:contentReference") && re.hasProperty(Constants.NODE)) {
                        try {
                            if (JCRContentUtils.isNodeType((JCRNodeWrapper) re.getProperty(Constants.NODE).getNode(), filteredTypes)) {
                                res.add(re);
                            }
                        } catch (ItemNotFoundException e) {
                            // referenced node does not exist anymore: display empty ref
                            res.add(re);
                        }
                    }
                }
            }

            if(res != null) {
                pageContext.setAttribute(var, res, scope);
            } else {
                pageContext.setAttribute(var, list, scope);
            }
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetState();
        }
        return super.doEndTag();
    }

    @Override
    protected void resetState() {
        list = null;
        node = null;
        properties = null;
        types = null;
        scope = PageContext.PAGE_SCOPE;
        var = null;
        super.resetState();
    }
    
    private String getValueOrDefaultIfMissingOrEmpty(final JSONObject jsonObject, final String paramName, final String defaultValue) {
        String paramValue = null;
        try {
            paramValue = (String) jsonObject.get(paramName);
        } catch (JSONException e) {
            // ignore exception indicating missing value, this is an annoying idiom of the JSON.org API, would be better if it conformed to returning null if no value was associated to the key
        }
        return (paramValue == null || paramValue.isEmpty()) ? defaultValue : paramValue;
    }
}