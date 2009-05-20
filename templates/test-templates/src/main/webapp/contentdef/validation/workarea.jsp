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
  <fmt:message key="description.template.validation"/>
</div>
<h3>definitions.cnd</h3>
<template:containerList name="validationFieldsUsingDefinitions" id="validationFieldsList" actionMenuNamePostFix="textContainers"
    actionMenuNameLabelKey="textContainers.add">
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
</template:containerList>

<h3>Struts Validator</h3>
<template:containerList name="validationFieldsUsingValidator" id="validationFieldsList" actionMenuNamePostFix="textContainers"
    actionMenuNameLabelKey="textContainers.add">
  <jsp:include page="../../common/displayContainerWithSub.jsp" flush="true"/>
</template:containerList>
