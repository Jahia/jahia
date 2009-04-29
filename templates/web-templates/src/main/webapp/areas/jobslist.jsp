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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../common/declarations.jspf" %>

                <template:include page="common/breadcrumb.jsp"/>
        <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}" escapeXml="false"/></h2>
        <template:include page="modules/introduction/introductionDisplay.jsp"/>
<!--start jobsSearchForm -->
		<div class="jobsSearchForm">
		<template:jahiaPageForm name="jobPageForm" method="get">
			<fieldset><legend><fmt:message key='jobSearchForm'/></legend>
			<p class="jobsSearchKeyword"><label for="jobsSearchKeyword"><fmt:message key='job.keywordSearch'/></label>
			<input type="text" name="jobsSearchKeyword" id="jobsSearchKeyword" class="field jobsSearchKeyword" value="${param.jobsSearchKeyword}" tabindex="4" /></p>
			<div class="divButton"><input type="submit" name="submit" id="submit" class="button" value="<fmt:message key="search"/>" tabindex="5" /></div>
			</fieldset>
		</template:jahiaPageForm>
		</div>
<!--stop jobsSearchForm -->
        <template:include page="modules/job/jobDisplay.jsp"/>
