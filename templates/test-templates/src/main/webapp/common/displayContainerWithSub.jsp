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