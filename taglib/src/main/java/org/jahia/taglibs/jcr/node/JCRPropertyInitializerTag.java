/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.jcr.node;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.Patterns;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import java.util.*;

/**
 * This Tag allows access to specific property of a node.
 * <p/>
 *
 * @author cmailleux
 */
public class JCRPropertyInitializerTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 3235254134426302521L;
    private transient static Logger logger = LoggerFactory.getLogger(JCRPropertyInitializerTag.class);
    private JCRNodeWrapper node;
    private String nodeType;
    private String name;
    private String var;
    private String initializers;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
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
        try {
            ExtendedNodeType type = null;
            if (nodeType != null) {
                type = NodeTypeRegistry.getInstance().getNodeType(nodeType);
            } else if (node != null) {
                type = node.getPrimaryNodeType();
            }
            if (type != null) {
                final List<ExtendedItemDefinition> extendedItemDefinitionList = type.getItems();
                for (ExtendedItemDefinition definition : extendedItemDefinitionList) {
                    if (definition.getName().equals(name)) {
                        Map<String, String> map;
                        if(initializers==null) {
                            map = definition.getSelectorOptions();
                        } else {
                            map = new LinkedHashMap<String, String>();
                            String[] strings = Patterns.COMMA.split(initializers);
                            for (String string : strings) {
                                map.put(string, "");
                            }
                        }
                        if (map.size() > 0) {
                            final Map<String, ChoiceListInitializer> initializers = ChoiceListInitializerService.getInstance().getInitializers();
                            List<ChoiceListValue> listValues = null;
                            final HashMap<String, Object> context = new HashMap<String, Object>();
                            context.put("contextNode",node);
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                if (initializers.containsKey(entry.getKey())) {
                                    listValues = initializers.get(entry.getKey()).getChoiceListValues(
                                            (ExtendedPropertyDefinition) definition, entry.getValue(), listValues,
                                            getRenderContext().getMainResourceLocale(), context
                                    );
                                }
                            }
                            if (listValues != null) {
                                pageContext.setAttribute(var, listValues);
                            }
                        } else if (definition instanceof ExtendedPropertyDefinition) {
                            JahiaTemplatesPackage pkg = ((ExtendedPropertyDefinition) definition).getDeclaringNodeType().getTemplatePackage();
                            ResourceBundle rb = ResourceBundles.get(pkg != null ? pkg : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("default"), getRenderContext().getMainResourceLocale());
                            final ExtendedPropertyDefinition propertyDefinition = (ExtendedPropertyDefinition) definition;
                            List<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
                            String resourceBundleKey = definition.getResourceBundleKey();
                            for (String value : propertyDefinition.getValueConstraints()) {
                                String display = Messages.get(rb, resourceBundleKey + "." + JCRContentUtils.replaceColon(value), value);
                                listValues.add(new ChoiceListValue(display, null, new ValueImpl(value, propertyDefinition.getRequiredType())));
                            }
                            pageContext.setAttribute(var, listValues);
                        }
                    }
                }

            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } finally {
            resetState();
        }
        return EVAL_PAGE;
    }

    /**
     * Specify the name of the property you want to get value of.
     *
     * @param name the name of the property you want to get value of
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

    public void setInitializers(String initializers) {
        this.initializers = initializers;
    }

    @Override
    protected void resetState() {
        name = null;
        node = null;
        nodeType = null;
        var = null;
        super.resetState();
    }
}
