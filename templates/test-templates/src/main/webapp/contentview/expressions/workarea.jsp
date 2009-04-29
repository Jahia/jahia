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
<%@ include file="../../common/declarations.jspf"%>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div> 
<div class="expectedResult">
  <fmt:message key="description.template.contentExpressions.expectedResult"/>
</div>
<div>
<fmt:message key="username"/>: ${currentUser.username}<br/>
<fmt:message key="label.siteKey"/>: ${fn:escapeXml(currentSite.siteKey)}<br/>
<fmt:message key="label.homepage.title"/>: ${fn:escapeXml(currentSite.homePage.title)}<br/>
<fmt:message key="label.homepage.id"/>: ${currentSite.homePage.pageID}<br/>
<fmt:message key="label.currentPage.id"/>: ${currentPage.pageID}<br/>
<fmt:message key="label.currentPage.languageStates"/>: ${currentPage.languageStates}<br/>
<fmt:message key="label.containerList.size"/>: ${currentPage.containerLists.maincontent.size}<br/>
<fmt:message key="label.containerList.size.alt"/>: ${currentPage.containerLists['maincontent'].size}<br/>
<fmt:message key="label.container.name"/>: ${currentPage.containerLists.maincontent.name}<br/>

<c:set var="firstContainer" value="${currentPage.containerLists.maincontent.containers['0']}"/>
<fmt:message key="label.firstContainer.id"/>:${firstContainer.ID}<br/>
<fmt:message key="label.firstContainer.title"/>: ${fn:escapeXml(firstContainer.fields['maincontent_mainContentTitle'].value)}<br/> 

<fmt:message key="label.container.name"/>: ${currentPage.containerLists.maincontent.name}<br/>
<c:forEach var="maincontentContainer" items="${currentPage.containerLists.maincontent}" varStatus="iterationStatus">
 <fmt:message key="label.container"><fmt:param value="${iterationStatus.count}"/></fmt:message> <fmt:message key="label.title"/>: ${fn:escapeXml(maincontentContainer.fields.maincontent_mainContentTitle.value)}<br/>
</c:forEach>  
</div>