/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.jcr.file;

import org.apache.struts.taglib.TagUtils;
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

        Map<String, Object> categoryDataMap = (Map<String, Object>) s.getAttribute("categoriesSelect.categoriesDataMap." + contextId);
        if (categoryDataMap == null) {
            categoryDataMap = new HashMap<String, Object>();
            s.setAttribute("categoriesSelect.categoriesDataMap." + contextId, categoryDataMap);
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
