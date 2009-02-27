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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ include file="declarations.jspf"%>

<c:choose>
  <c:when test="${empty param.level}" >
    <c:set var="level" value="1"/>
  </c:when>
  <c:otherwise>
    <c:set var="level" value="${param.level}"/>
  </c:otherwise>
</c:choose>

<template:container id="testSubcontainer${level}" cache="${functions:default(param.activateFragmentCache, 'true')}">
  <div class="level${level}">
    <div class="containerTitle">
      <fmt:message key="containerListLevel">
        <fmt:param value="${level}"/>
      </fmt:message>
    </div>
    
    <jsp:include page="displayFields.jsp" flush="true">
      <jsp:param name="containerId" value="testSubcontainer${level}"/>
    </jsp:include>
    
    <c:set var="nextContainerListName" value="${level}_listName"/>    
    <c:set var="nextActionMenuName" value="${level}_actionMenuName"/>
  </div>        
  <c:forEach items="${paramValues[nextContainerListName]}" var="currentContainerListName" varStatus="iteratorStatus">
    <template:containerList name="${currentContainerListName}" id="testContainerList${level}" actionMenuNamePostFix="${paramValues[nextActionMenuName][iteratorStatus.count - 1]}"
        actionMenuNameLabelKey="${paramValues[nextActionMenuName][iteratorStatus.count - 1]}.add">
      <jsp:include page="displayContainerWithSub.jsp" flush="true">
        <jsp:param name="level" value="${level + 1}"/>
      </jsp:include>           
    </template:containerList>
  </c:forEach>
</template:container>