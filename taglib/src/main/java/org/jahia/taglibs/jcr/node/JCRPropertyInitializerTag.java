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
package org.jahia.taglibs.jcr.node;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import java.util.List;
import java.util.Map;

/**
 * This Tag allows access to specific property of a node.
 * <p/>
 *
 * @author cmailleux
 */
public class JCRPropertyInitializerTag extends AbstractJahiaTag {
    private transient static Logger logger = Logger.getLogger(JCRPropertyInitializerTag.class);
    private JCRNodeWrapper node;
    private String name;
    private String var;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    /**
     * Default processing of the start tag, returning SKIP_BODY.
     *
     * @return SKIP_BODY
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        if (node != null) {
            try {
                final JCRPropertyWrapper jcrPropertyWrapper = node.getProperty(name);
                ExtendedPropertyDefinition definition = (ExtendedPropertyDefinition) jcrPropertyWrapper.getDefinition();
                final Map<String, String> map = definition.getSelectorOptions();
                if (map.size() > 0) {
                    final Map<String, ChoiceListInitializer> initializers = ChoiceListInitializerService.getInstance().getInitializers();
                    List<ChoiceListValue> listValues = null;
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (initializers.containsKey(entry.getKey())) {
                            listValues = initializers.get(entry.getKey()).getChoiceListValues(
                                    Jahia.getThreadParamBean(), definition, entry.getValue(),
                                    node.getPrimaryNodeTypeName(), listValues);
                        }
                    }
                    if(listValues!=null) {
                        pageContext.setAttribute(var,listValues);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return EVAL_PAGE;
    }

    /**
     * Specify the name of the property you want to get value of.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * If you do not want to output directly the value of the property (call javax.jcr.Value.getString())
     * The define a value for this.
     *
     * @param var The name in the pageContext in which you will find the javax.jcr.Value or javax.jcr.Value[] object associated with this property
     */
    public void setVar(String var) {
        this.var = var;
    }
}