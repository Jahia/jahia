<%@ tag import="org.jahia.params.ProcessingContext" %>
<%@ tag import="org.jahia.ajax.gwt.subengines.categoriespicker.client.CategoriesPickerEntryPoint" %>
<%@ tag import="java.util.Iterator" %>
<%@ tag import="org.jahia.services.categories.Category" %>
<%@ tag import="org.jahia.engines.shared.Category_Field" %>
<%@ tag import="java.util.HashMap" %>
<%@ tag import="java.util.List" %>
<%@ tag import="org.jahia.data.JahiaData" %>
<%@ tag import="org.jahia.engines.EngineLanguageHelper" %>
<%@ tag import="org.jahia.engines.JahiaEngine" %>
<%@ tag import="org.jahia.data.fields.JahiaField" %>
<%@ tag import="org.jahia.services.usermanager.JahiaUser" %>

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
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ attribute name="startCategoryKey" required="false" rtexprvalue="true" type="java.lang.String"
              description="root category key" %>
<%@ attribute name="readonly" required="false" rtexprvalue="true" type="java.lang.Boolean"
              description="True value allows to pick several categories " %>
<%@ attribute name="multiple" required="false" rtexprvalue="true" type="java.lang.Boolean"
              description="True value allows to pick several categories " %>
<%@ attribute name="selectedCategories" required="false" rtexprvalue="true" type="java.util.List"
              description="a list of the selececCategories" %>
<%@ attribute name="locale" required="false" rtexprvalue="true" type="java.lang.String"
              description="the locale used for displaying the category title" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaUser currentUser = jParams.getUser();
    final boolean hasRootCategoryAccess = Category.getRootCategory(currentUser) != null;
    if (!hasRootCategoryAccess) { %>
<utility:resourceBundle resourceBundle="JahiaAdministrationResources"
        resourceName="org.jahia.actions.server.admin.categories.ManageCategories.rootAccessDenied"/>
<%} else {%>
<logic:present name="org.jahia.engines.EngineHashMap" scope="request">
    <script type="text/javascript">
        var sCategories = [
            <%
    if(selectedCategories != null){
        final Iterator selectedCa = selectedCategories.iterator();
        while (selectedCa.hasNext()) {
            final String key = (String) selectedCa.next();
            final Category cat = Category.getCategory(key, jParams.getUser());
            if (cat != null) {
                String title = cat.getTitle(jParams.getLocale());
                if (title == null || title.length() == 0) {
                 title = key;
                }%>
            {"id":"<%=cat.getJahiaCategory().getId()%>","key":"<%=key%>","title":"<%=title%>","path":"<%=cat.getCategoryPath(jParams.getUser())%>"}<%if(selectedCa.hasNext()){%>,<%}%>
            <%
            }
        }
    }%>];
        var sLocale = '${locale}';
    </script>
</logic:present>
<input id="removedCategories" type="hidden" name="removedCategories" value=""/>

<template:gwtJahiaModule id="<%=CategoriesPickerEntryPoint.ID%>" jahiaType="<%=CategoriesPickerEntryPoint.ID%>"
                         rootKey="${startCategoryKey}" readonly="${readonly}">
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.categories"
                                     aliasResourceName="categories"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.title"
                                     aliasResourceName="title"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.path"
                                     aliasResourceName="path"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.search"
                                     aliasResourceName="search"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.remove"
                                     aliasResourceName="remove"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.add"
                                     aliasResourceName="add"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.property"
                                     aliasResourceName="property"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.name"
                                     aliasResourceName="name"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.key"
                                     aliasResourceName="key"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.value"
                                     aliasResourceName="value"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.categories.path"
                                     aliasResourceName="path"/>
</template:gwtJahiaModule>
<%}%>