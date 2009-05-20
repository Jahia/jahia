<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.paste.label"
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
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.cut.label"
                            aliasResourceName="cat_cut"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.deleteCategory.confirm.label"
                            aliasResourceName="cat_delete_confirm"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.deleteRootCategory.confirm.label"
                            aliasResourceName="cat_deleteRoot_confirm"/>
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