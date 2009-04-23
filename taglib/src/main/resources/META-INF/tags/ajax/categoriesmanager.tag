<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag import="org.jahia.params.ProcessingContext" %>
<%@ tag import="org.jahia.bin.JahiaAdministration" %>
<%@ tag import="org.jahia.services.usermanager.JahiaUser" %>
<%@ tag import="org.jahia.services.categories.Category" %>
<%@ attribute name="startPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    final String importActionUrl = JahiaAdministration.composeActionURL(request, response, "categories", "&sub=import");
    final String exportUrl = jParams.composeSiteUrl(jParams.getSite()) + "/engineName/export/categories.xml?exportformat=cats";
    final JahiaUser currentUser = jParams.getUser();
    final boolean hasRootCategoryAccess = Category.getRootCategory(currentUser) != null;
    if (!hasRootCategoryAccess) { %>
<fmt:message key="org.jahia.actions.server.admin.categories.ManageCategories.rootAccessDenied"/>
<%} else {%>
<template:gwtJahiaModule id="categories_manager" jahiaType="categories_manager" importAction="<%=importActionUrl%>"
                         exportUrl="<%=exportUrl%>"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.addNewCategory.label"
                            aliasResourceName="cat_create"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.edit.label"
                            aliasResourceName="cat_update"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.deleteCategoryAction.label"
                            aliasResourceName="cat_remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.include.actionSelector.RightsMgmt.label"
                            aliasResourceName="cat_update_acl"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.moveCategoryAction.label"
                            aliasResourceName="cat_paste"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.copy.label"
                            aliasResourceName="cat_copy"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.site.ManageSites.export.label"
                            aliasResourceName="cat_export"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.site.ManageSites.import.label"
                            aliasResourceName="cat_import"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.parentCategoryKey.label"
        aliasResourceName="cat_parentkey"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.informations.label"
        aliasResourceName="cat_info"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.path.label"
                            aliasResourceName="cat_path"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.properties.add.label"
        aliasResourceName="cat_prop_add"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.sitepermissions.permission.engines.audit.ManageLogs_Engine.label"
        aliasResourceName="cat_log"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.properties.remove.label"
        aliasResourceName="cat_prop_remove"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.properties.label"
        aliasResourceName="cat_prop"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.title.label"
                            aliasResourceName="cat_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.titles.label"
                            aliasResourceName="cat_titles"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.key.label"
                            aliasResourceName="cat_key"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.copyCategoryAction.label"
                            aliasResourceName="cat_cut"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.propertyName.label"
        aliasResourceName="cat_prop_name"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.propertyValue.label"
        aliasResourceName="cat_prop_value"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.deleteProperty.label"
        aliasResourceName="title_remove_prop"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.editCategory.saveProperties.label"
        aliasResourceName="title_new_prop"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.save.label" aliasResourceName="button_save"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.admin.categories.ManageCategories.deleteCategoryAction.label"
        aliasResourceName="button_remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.update.label" aliasResourceName="button_update"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.cancel.label" aliasResourceName="button_cancel"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.apply.label" aliasResourceName="button_apply"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                            aliasResourceName="ae_principal"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                            aliasResourceName="ae_restore_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label"
                            aliasResourceName="ae_inherited_from"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inherited.label"
                            aliasResourceName="ae_inherited"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"
                            aliasResourceName="ae_restore_all_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label"
                            aliasResourceName="ae_break_all_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                            aliasResourceName="ae_remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label"
                            aliasResourceName="ae_save"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label"
                            aliasResourceName="ae_restore"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                            aliasResourceName="um_adduser"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                            aliasResourceName="um_addgroup"/>

<%}%>