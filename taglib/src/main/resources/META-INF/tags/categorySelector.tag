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

<%@ tag body-content="empty" description="Renders the link to the category selection engine (as a popup window)." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted category value with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do show the include children checkbox." %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children input field." %>
<%@ attribute name="root" type="java.lang.String" description="The root category to start with." %>
<%@ attribute name="autoSelectParent" required="false" rtexprvalue="true" type="java.lang.Boolean"
              description="Allows to control if we have to auto check the parent of a category when selected or not. [false]" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="displayIncludeChildren" value="${functions:default(displayIncludeChildren, true)}"/>
<c:set var="autoSelectParent" value="${functions:default(autoSelectParent, false)}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren"
                                                    value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in sub-categories --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${functions:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<c:set var="root" value="${not empty root ? root : 'root'}"/>
&nbsp;<a href="#select"
onclick="javascript:{var categoriesSelector = window.open('${pageContext.request.contextPath}/engines/categories/launcher.jsp?autoSelectParent=${autoSelectParent}&pid=${jahia.page.ID}&contextId=${fieldId}@${root}&selectedCategories=' + document.getElementById('${fieldId}').value, '<%="categoriesSelector_" + session.getId().replaceAll("[^a-zA-Z0-9]", "_")%>', 'resizable=yes,scrollbars=yes,height=800,width=600'); categoriesSelector.focus(); return false;}"
title='<utility:resourceBundle resourceName="selectors.categorySelector.selectCategories"
                                      defaultValue="Select categories"/>'><utility:resourceBundle  resourceName="selectors.select" defaultValue="select"/></a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}"><utility:resourceBundle resourceName="selectors.categorySelector.selectCategories.includeChildren" defaultValue="include subcategories"/></label>
</c:if>
