<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ tag import="org.jahia.params.ProcessingContext" %>
<%@ tag import="java.util.Iterator" %>
<%@ tag import="org.jahia.services.categories.Category" %>
<%@ tag import="org.jahia.services.usermanager.JahiaUser" %>
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

<template:gwtJahiaModule id="categories_picker" jahiaType="categories_picker"
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
<internal:gwtResourceBundle resourceName="org.jahia.admin.categories.ManageCategories.editCategory.informations.label"
                            aliasResourceName="information"/>

<%}%>