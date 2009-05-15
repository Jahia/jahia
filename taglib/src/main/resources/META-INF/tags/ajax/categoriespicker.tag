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
<%@ tag import="java.util.Iterator" %>
<%@ tag import="org.jahia.services.categories.Category" %>
<%@ tag import="org.jahia.services.usermanager.JahiaUser" %>
<%@ tag import="org.jahia.ajax.gwt.module.categorypicker.client.CategoryPickerEntryPoint" %>
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
<%@ attribute name="autoSelectParent" required="false" rtexprvalue="true" type="java.lang.String"
              description="allows to control if we have to auto check the parent of a category when selected or not." %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaUser currentUser = jParams.getUser();
    final boolean hasRootCategoryAccess = Category.getRootCategory(currentUser) != null;
    if (!hasRootCategoryAccess) { %>
<fmt:message key="org.jahia.actions.server.admin.categories.ManageCategories.rootAccessDenied"/>
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

        var sAutoSelectParent = '${autoSelectParent}';

    </script>
</logic:present>
<input id="removedCategories" type="hidden" name="removedCategories" value=""/>

<template:gwtJahiaModule id="<%=CategoryPickerEntryPoint.ID%>" jahiaType="<%=CategoryPickerEntryPoint.ID%>"
                         rootKey="${startCategoryKey}" readonly="${readonly}" multiple="${multiple}"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.categories"
                            aliasResourceName="categories"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.title"
                            aliasResourceName="title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.path"
                            aliasResourceName="path"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.search"
                            aliasResourceName="search"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.remove"
                            aliasResourceName="remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.add"
                            aliasResourceName="add"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.property"
                            aliasResourceName="property"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.name"
                            aliasResourceName="name"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.key"
                            aliasResourceName="key"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.value"
                            aliasResourceName="value"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.categories.path"
                            aliasResourceName="path"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.selectedCategories.label"
                            aliasResourceName="categories_selected"/>
<%}%>