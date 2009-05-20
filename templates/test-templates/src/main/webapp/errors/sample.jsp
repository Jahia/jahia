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
<%@ include file="../common/declarations.jspf"%>

<div class="expectedResultTitle">
  <fmt:message key="label.expected.result"/>:
</div> 
<div class="expectedResult">
  <fmt:message key="description.template.errors.expectedResult"/>
</div>
<h3>Following links lead to error pages (will open in a new window):</h3>
<ul>
  <li><a href="<c:url value='/ajaxaction/GetQuotedPrintable'/>" target="_blank">Default '400 Bad request' page</a></li>  
  <li><a href="<c:url value='ajaxaction/GetWorkFlowChildObjectsEntryPoint'/>" target="_blank">Custom '404 Not found' page</a></li>  
</ul>