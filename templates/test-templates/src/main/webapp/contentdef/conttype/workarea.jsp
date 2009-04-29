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
  <fmt:message key="description.template.containerTypes.expectedResult"/>
</div>

<template:containerList name="singleContainer" id="testContainerListSingle" actionMenuNamePostFix="testSingleContainers"
    actionMenuNameLabelKey="testSingleContainers.add">
    
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true">
    <jsp:param name="1_listName" value="singleSubContainer"/>
    <jsp:param name="1_listName" value="mandatorySubContainer"/>    
    <jsp:param name="1_listName" value="singleMandatorySubContainer"/>    
    <jsp:param name="2_listName" value="singleSubSubContainer"/>
    <jsp:param name="2_listName" value="mandatorySubSubContainer"/>
    <jsp:param name="2_listName" value="singleMandatorySubSubContainer"/>
    <jsp:param name="1_actionMenuName" value="testSingleSubContainers"/>
    <jsp:param name="1_actionMenuName" value="testMandatorySubContainers"/>
    <jsp:param name="1_actionMenuName" value="testSingleMandatorySubContainers"/>        
    <jsp:param name="2_actionMenuName" value="testSingleSubSubContainers"/>
    <jsp:param name="2_actionMenuName" value="testMandatorySubSubContainers"/>
    <jsp:param name="2_actionMenuName" value="testSingleMandatorySubSubContainers"/>    
  </jsp:include>       
</template:containerList>
    
<template:containerList name="mandatoryContainer" id="testContainerListMandatory" actionMenuNamePostFix="testMandatoryContainers"
    actionMenuNameLabelKey="testMandatoryContainers.add">
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
</template:containerList>

<template:containerList name="singleMandatoryContainer" id="testContainerListSingleMandatory" actionMenuNamePostFix="testSingleMandatoryContainers"
    actionMenuNameLabelKey="testSingleMandatoryContainers.add">
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
</template:containerList>