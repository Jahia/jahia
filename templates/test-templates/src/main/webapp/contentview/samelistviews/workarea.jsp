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
        <query:selector selectorName="testContainerList6" nodeTypeName="test_templates:someFields"/>
        <query:sortBy propertyName="created" order="${queryConstants.ORDER_DESCENDING}" numberValue="false"  metadata="true"/>
      </query:containerQuery>
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
  <br/>
  <h3><fmt:message key="label.listWithWindowSizeAndSortByTextAndPaginationOnTop"><fmt:param value="4"/></fmt:message>:</h3><br/>
  <template:containerList name="someFields" id="testContainerList8" windowSize="4" displayPagination="top" 
      displayActionMenu="false">
      <query:containerQuery> 
        <query:selector selectorName="testContainerList6" nodeTypeName="test_templates:someFields"/>
        <query:sortBy propertyName="smallText" order="${queryConstants.ORDER_ASCENDING}" numberValue="false"/>
      </query:containerQuery>
    <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
  </template:containerList>
<br/>
<h3><fmt:message key="label.absoluteListWithWindowSizeAndNbStepsPerPage"><fmt:param value="1"/><fmt:param value="2"/></fmt:message>:</h3><br/>
<template:absoluteContainerList pageId="${currentPage.ID}" name="someFields" id="testContainerList9" windowSize="1" nbStepPerPage="2" displayActionMenu="false">
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
</template:absoluteContainerList>