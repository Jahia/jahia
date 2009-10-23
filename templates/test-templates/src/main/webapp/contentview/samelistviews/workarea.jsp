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
<%@ include file="../../common/declarations.jspf"%>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div> 
<div class="expectedResult">
  <fmt:message key="description.template.sameListViews.expectedResult"/>
</div>
<div>
  <h3><fmt:message key="label.entireList"><fmt:param><c:if test="${currentRequest.editMode}"><fmt:message key="label.andEdit"/>&nbsp;</c:if></fmt:param></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList"
      actionMenuNamePostFix="testContainers" actionMenuNameLabelKey="testContainers.add">
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithWindowAndMaxSizeAndPagination"><fmt:param value="2"/><fmt:param value="5"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList2" windowSize="2" maxSize="5"
      displayActionMenu="false">
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithWindowOffsetAndSizeAndTwoPaginations"><fmt:param value="4"/><fmt:param value="4"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList3" windowOffset="4" windowSize="4" displayPagination="top,bottom"
      displayActionMenu="false">
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithWindowOffsetAndSizeAndPaginationHidden"><fmt:param value="4"/><fmt:param value="4"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList4" windowOffset="4" windowSize="4" displayPagination="false"
      displayActionMenu="false">
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithMaxSize"><fmt:param value="3"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList5" maxSize="3"
      displayActionMenu="false">
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithRandomContainers"><fmt:param value="2"/></fmt:message>:</h3><br/>
  <template:randomContainer displayedContainer="2">
    <template:containerList name="someFields" id="testContainerList6"
        displayActionMenu="false">
      <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true">
        <jsp:param name="activateFragmentCache" value="false"/>
      </jsp:include>
    </template:containerList>
  </template:randomContainer>
  <br/>
  <h3><fmt:message key="label.listWithMaxSizeAndSortByDate"><fmt:param value="3"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList7" maxSize="3" 
      displayActionMenu="false">
      <query:containerQuery> 
        <query:selector selectorName="testContainerList7" nodeTypeName="test_templates:someFields"/>
        <query:sortBy selectorName="testContainerList7" propertyName="created" order="${queryConstants.ORDER_DESCENDING}"/>
      </query:containerQuery>
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithWindowSizeAndSortByTextAndPaginationOnTop"><fmt:param value="4"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList8" windowSize="4" displayPagination="top" 
      displayActionMenu="false">
      <query:containerQuery> 
        <query:selector selectorName="testContainerList8" nodeTypeName="test_templates:someFields"/>
        <query:sortBy selectorName="testContainerList8" propertyName="smallText" order="${queryConstants.ORDER_ASCENDING}"/>
      </query:containerQuery>
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
<br/>
<h3><fmt:message key="label.absoluteListWithWindowSizeAndNbStepsPerPage"><fmt:param value="1"/><fmt:param value="2"/></fmt:message>:</h3><br/>
</div>
<template:absoluteContainerList pageId="${currentPage.ID}" name="someFields" id="testContainerList9" windowSize="1" nbStepPerPage="2" displayActionMenu="false">
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
</template:absoluteContainerList>