/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.jcr.file;

import org.apache.struts.taglib.TagUtils;
import org.jahia.engines.categories.CategoriesSelect_Engine;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.nodetypes.*;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 déc. 2007
 * Time: 18:26:37
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PropertyInputTag extends PropertyValueTag {
    private String name = "propertyDefinition";
    private String property;
    private String scope;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int doStartTag() throws JspException {
        ExtendedItemDefinition itemDef = (ExtendedItemDefinition) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        boolean jprot = itemDef.isProtected();

        if (!jprot) {
            jspSuffix = "_edit.jsp";
        }

        return super.doStartTag();
    }

    protected void handleTextField(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        if (propDef.isProtected()) {
            super.handleTextField(propDef, objectNode, provider);
            return;
        }
        if (propDef.getSelector() == SelectorType.CHOICELIST) {
            handleSelectMenu(propDef, objectNode);
            return;
        }

        String name = propDef.getName();

        String value = null;
        if (objectNode != null && objectNode.hasProperty(name)) {
            value = objectNode.getProperty(name).getString();
        }

        name = name.replace(':','_');
        JspWriter out = pageContext.getOut();
        if (propDef.getSelector() == SelectorType.RICHTEXT) {
            // fck editor ..
            out.print("<textarea cols=\"30\" name=\"prop_"+name+"\">"+ ((value == null) ? "" : value) +"</textarea>");
        } else {
            out.print("<input name=\"prop_"+name+"\" value=\""+ ((value == null) ? "" : value) +"\" size=\"30\"/>");
        }
    }

    protected void handleMultipleTextField(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        if (propDef.isProtected()) {
            super.handleMultipleTextField(propDef, objectNode, provider);
            return;
        }
        if (propDef.getSelector() == SelectorType.CHOICELIST) {
            handleSelectMenu(propDef, objectNode);
            return;
        }

        // .. ?
    }

    protected void handleSelectMenu(PropertyDefinition propDef, Node objectNode) throws Exception {
        ServletRequest request = pageContext.getRequest();

        String name = propDef.getName();

        String value = null;
        if (objectNode != null && objectNode.hasProperty(name)) {
            value = objectNode.getProperty(name).getString();
        }

        name = name.replace(':','_');
        String[] cs = propDef.getValueConstraints();

        request.setAttribute("multiple", Boolean.valueOf(propDef.isMultiple()));
        request.setAttribute("propertyName", name);
        request.setAttribute("value", value);
        request.setAttribute("values", cs);

        pageContext.include("/engines/filemanager/types/selectmenu_edit.jsp");
    }

    protected void handleCategory(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        if (propDef.isProtected()) {
            super.handleCategory(propDef, objectNode, provider);
            return;
        }

        ServletRequest request = pageContext.getRequest();


        String name = propDef.getName();
        List<String> selected = new ArrayList<String>();
        if (objectNode != null && objectNode.hasProperty(name)) {
            if (propDef.isMultiple()) {
                Value[] values = objectNode.getProperty(name).getValues();
                for (int i = 0; i < values.length; i++) {
                    String value = values[i].getString();
                    value = Category.getCategoryKey(value);
                    selected.add(value);
                }
            } else {
                String value = objectNode.getProperty(name).getString();
                value = Category.getCategoryKey(value);
                selected.add(value);
            }
        }
        name = name.replace(':','_');

        String root = propDef.getSelectorOptions().get("root");
        if (root == null) {
            root = "root";
        }

        String contextId = name + "@" + root;
        HttpSession s = pageContext.getSession();

        Map<String, Object> categoryDataMap = (Map<String, Object>) s.getAttribute(CategoriesSelect_Engine.ENGINE_NAME + ".categoriesDataMap." + contextId);
        if (categoryDataMap == null) {
            categoryDataMap = new HashMap<String, Object>();
            s.setAttribute(CategoriesSelect_Engine.ENGINE_NAME + ".categoriesDataMap." + contextId, categoryDataMap);
        }

        String selectedIds = "";
        for (Iterator<String> iterator = selected.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            Category c = Category.getCategory(key);
            selectedIds += (new Integer(c.getObjectKey().getIdInType()));
            if (iterator.hasNext()) {
                selectedIds += ",";
            }
        }

        categoryDataMap.put("rootCategoryKey", root);
        categoryDataMap.put("defaultSelectedCategories",selected);
        request.setAttribute("selectedCategories",selected);
        request.setAttribute("selectedCategoriesId",selectedIds);
        request.setAttribute("propertyName", name);
        request.setAttribute("contextId", contextId);

        pageContext.include("/engines/filemanager/types/category_edit.jsp");
    }

    protected void handleBoolean(ExtendedPropertyDefinition propDef, Node objectNode, JCRStoreProvider provider) throws Exception {
        if (propDef.isProtected()) {
            super.handleBoolean(propDef, objectNode, provider);
            return;
        }

        String name = propDef.getName();

        boolean value = false;
        if (objectNode != null && objectNode.hasProperty(name)) {
            value = objectNode.getProperty(name).getBoolean();
        }

        name = name.replace(':','_');
        JspWriter out = pageContext.getOut();
        out.print("<input name=\"propset_"+name+"\" type=\"hidden\" value=\"on\" />");
        out.print("<input name=\"prop_"+name+"\" type=\"checkbox\"");
        if (value) {
            out.print(" checked=\"checked\"");
        }
        out.print("/>");
    }

}
