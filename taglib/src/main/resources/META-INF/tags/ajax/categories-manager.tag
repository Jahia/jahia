<%@ tag import="org.jahia.params.ProcessingContext" %>
<%@ tag import="org.jahia.bin.JahiaAdministration" %>
<%@ tag import="org.jahia.resourcebundle.JahiaResourceBundle" %>
<%@ tag import="org.jahia.services.usermanager.JahiaUser" %>
<%@ tag import="org.jahia.services.categories.Category" %>
<%--
Copyright 2002-2007 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>

<%@ attribute name="startPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    final String importActionUrl = JahiaAdministration.composeActionURL(request, response, "categories", "&sub=import");
    final String exportUrl = jParams.composeSiteUrl(jParams.getSite()) + "/engineName/export/categories.xml?exportformat=cats";
    final JahiaUser currentUser = jParams.getUser();
    final boolean hasRootCategoryAccess = Category.getRootCategory(currentUser) != null;
    if (!hasRootCategoryAccess) { %>
<utility:resourceBundle resourceBundle="JahiaAdministrationResources"
        resourceName="org.jahia.actions.server.admin.categories.ManageCategories.rootAccessDenied"/>
<%} else {%>
<template:gwtJahiaModule id="categories_manager" jahiaType="categories_manager" importAction="<%=importActionUrl%>"
                         exportUrl="<%=exportUrl%>">
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.addNewCategory.label"
                                    aliasResourceName="cat_create"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.edit.label"
                                    aliasResourceName="cat_update"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.deleteCategoryAction.label"
            aliasResourceName="cat_remove"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.include.actionSelector.RightsMgmt.label"
                                     aliasResourceName="cat_update_acl"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.moveCategoryAction.label"
                                    aliasResourceName="cat_paste"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.copy.label"
                                    aliasResourceName="cat_copy"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.site.ManageSites.export.label"
                                    aliasResourceName="cat_export"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.site.ManageSites.import.label"
                                    aliasResourceName="cat_import"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.parentCategoryKey.label"
            aliasResourceName="cat_parentkey"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.informations.label"
            aliasResourceName="cat_info"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.path.label"
                                    aliasResourceName="cat_path"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.properties.add.label"
            aliasResourceName="cat_prop_add"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.sitepermissions.permission.engines.audit.ManageLogs_Engine.label"
            aliasResourceName="cat_log"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.properties.remove.label"
            aliasResourceName="cat_prop_remove"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.properties.label"
            aliasResourceName="cat_prop"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.title.label"
                                    aliasResourceName="cat_title"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.titles.label"
                                    aliasResourceName="cat_titles"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.key.label"
                                    aliasResourceName="cat_key"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.copyCategoryAction.label"
                                    aliasResourceName="cat_cut"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.propertyName.label"
            aliasResourceName="cat_prop_name"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.propertyValue.label"
            aliasResourceName="cat_prop_value"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.deleteProperty.label"
            aliasResourceName="title_remove_prop"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.editCategory.saveProperties.label"
            aliasResourceName="title_new_prop"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.save.label" aliasResourceName="button_save"/>
    <utility:gwtAdminResourceBundle
            resourceName="org.jahia.admin.categories.ManageCategories.deleteCategoryAction.label"
            aliasResourceName="button_remove"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.update.label" aliasResourceName="button_update"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.cancel.label" aliasResourceName="button_cancel"/>
    <utility:gwtAdminResourceBundle resourceName="org.jahia.admin.apply.label" aliasResourceName="button_apply"/>
</template:gwtJahiaModule>
<utility:gwtResourceBundleDictionary elementId="gwtacleditor" moduleType="gwtacleditor">
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                                     aliasResourceName="ae_principal"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                                     aliasResourceName="ae_restore_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label"
                                     aliasResourceName="ae_inherited_from"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"
                                     aliasResourceName="ae_restore_all_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label"
                                     aliasResourceName="ae_break_all_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                                     aliasResourceName="ae_remove"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label"
                                     aliasResourceName="ae_save"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label"
                                     aliasResourceName="ae_restore"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                                     aliasResourceName="um_adduser"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                                     aliasResourceName="um_addgroup"/>
</utility:gwtResourceBundleDictionary>
<%}%>