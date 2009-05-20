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
<%@ include file="../../common/declarations.jspf" %>
<div class="box4">
		<div class="box4-topright"></div>
		<div class="box4-topleft"></div>
		<h3 class="box4-header"><span class="jobsTitle"><fmt:message key='jobList'/></span></h3>
		<div class="box4-bottomright"></div>
		<div class="box4-bottomleft"></div>
	    <div class="clear"> </div>
</div>
<template:containerList name="job" id="jobContainer"
                       actionMenuNamePostFix="job" actionMenuNameLabelKey="job">
    <query:containerQuery>
        <query:selector nodeTypeName="web_templates:jobContainer" selectorName="jobSelector"/>
        <query:childNode selectorName="jobSelector" path="${jobContainer.JCRPath}"/>
        <c:if test="${!empty param.jobsSearchKeyword}">
            <query:fullTextSearch searchExpression="${param.jobsSearchKeyword}"/>
        </c:if>
    </query:containerQuery>
    <%@ include file="jobDisplay.jspf" %>
</template:containerList>