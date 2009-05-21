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
package org.jahia.taglibs.internal.categories;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;
import org.jahia.ajax.gwt.client.core.JahiaType;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 20 mai 2009
 * Time: 14:12:00
 */
public class CategoriesManagerTag extends AbstractJahiaTag {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CategoriesManagerTag.class);


    private String startPath;

    public String getStartPath() {
        return startPath;
    }

    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

    @Override
    public int doStartTag() throws JspException {
        super.doStartTag();
        final JspWriter out = pageContext.getOut();
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

        final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");

        final JahiaUser currentUser = jParams.getUser();
        try {
            final boolean hasRootCategoryAccess = Category.getRootCategory(currentUser) != null;
            if (!hasRootCategoryAccess) {

                out.print(getJahiaInternalResourceValue("org.jahia.actions.server.admin.categories.ManageCategories.rootAccessDenied"));
            } else {
                final String importActionUrl = JahiaAdministration.composeActionURL(request, response, "categories", "&sub=import");
                final String exportUrl = jParams.composeSiteUrl(jParams.getSite()) + "/engineName/export/categories.xml?exportformat=cats";
                Map<String, Object> extraParam = new HashMap<String, Object>();
                extraParam.put("importAction", importActionUrl);
                extraParam.put("exportUrl", exportUrl);

                // generate holder
                out.print(GWTIncluder.generateJahiaModulePlaceHolder(false, null, "categories_manager", "categories_manager", extraParam));


                // add gwt messgaes related to GWT
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.addNewCategory.label", "cat_create");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.edit.label", "cat_update");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.deleteCategoryAction.label", "cat_remove");
                addGWTInternalMessage("org.jahia.engines.include.actionSelector.RightsMgmt.label", "cat_update_acl");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.moveCategoryAction.label", "cat_paste");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.copy.label", "cat_copy");
                addGWTInternalMessage("org.jahia.admin.site.ManageSites.export.label", "cat_export");
                addGWTInternalMessage("org.jahia.admin.site.ManageSites.import.label", "cat_import");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.parentCategoryKey.label", "cat_parentkey");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.informations.label", "cat_info");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.path.label", "cat_path");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.properties.add.label", "cat_prop_add");
                addGWTInternalMessage("org.jahia.admin.sitepermissions.permission.engines.audit.ManageLogs_Engine.label", "cat_log");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.properties.remove.label", "cat_prop_remove");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.properties.label", "cat_prop");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.title.label", "cat_title");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.titles.label", "cat_titles");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.key.label", "cat_key");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.copyCategoryAction.label", "cat_cut");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.propertyName.label", "cat_prop_name");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.propertyValue.label", "cat_prop_value");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.deleteProperty.label", "title_remove_prop");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.editCategory.saveProperties.label", "title_new_prop");
                addGWTInternalMessage("org.jahia.admin.save.label", "button_save");
                addGWTInternalMessage("org.jahia.admin.categories.ManageCategories.deleteCategoryAction.label", "button_remove");
                addGWTInternalMessage("org.jahia.admin.update.label", "button_update");
                addGWTInternalMessage("org.jahia.admin.cancel.label", "button_cancel");
                addGWTInternalMessage("org.jahia.admin.apply.label", "button_apply");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.principal.label", "ae_principal");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.restoreInheritance.label", "ae_restore_inheritance");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.inheritedFrom.label", "ae_inherited_from");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.restoreAllInheritance.label", "ae_restore_all_inheritance");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.breakAllInheritance.label", "ae_break_all_inheritance");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.remove.label", "ae_remove");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.save.label", "ae_save");
                addGWTInternalMessage("org.jahia.engines.rights.ManageRights.restore.label", "ae_restore");
                addGWTInternalMessage("org.jahia.engines.users.SelectUG_Engine.newUsers.label", "um_adduser");
                addGWTInternalMessage("org.jahia.engines.users.SelectUG_Engine.newGroups.label", "um_addgroup");
            }
        } catch (Exception e) {
            logger.error(e, e);
        }

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        startPath = null;
        return SKIP_BODY;
    }

    /**
     * Add gwt message. The resource name comes from internal messages
     *
     * @param resourceName
     * @param gwtKey
     */
    private void addGWTInternalMessage(String resourceName, String gwtKey) {
        addGwtDictionaryMessage(gwtKey, getJahiaInternalResourceValue(resourceName));
    }


}
