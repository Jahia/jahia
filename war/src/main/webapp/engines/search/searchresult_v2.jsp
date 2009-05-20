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
<%@page language="java" contentType="text/html; charset=UTF-8" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" 
%><%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/andromeda.css" type="text/css"/>
    <title><fmt:message key="org.jahia.engines.search.Search_Engine.searchResultsTitle.label"/></title>
</head>
<body>
<center>
    <div style="width: 50%; text-align: left;">
    <s:results>
        <h1>Jahia</h1>
        <h3>Search Results: ${count} found</h3>
        <hr/>
        <div id="resultslist">
          <ol>
            <s:resultIterator>
            <li>
              <dl>
                <dt><a href="${hit.link}">${fn:escapeXml(hit.title)}</a></dt>
                <dd>${hit.summary}</dd>
                <dd>File format: ${hit.contentType}</dd>
                <dd>created: <fmt:formatDate value="${hit.created}" pattern="dd.MM.yyyy HH:mm"/>&nbsp;by&nbsp;${fn:escapeXml(hit.createdBy)}</dd>
                <dd>last modified: <fmt:formatDate value="${hit.lastModified}" pattern="dd.MM.yyyy HH:mm"/>&nbsp;by&nbsp;${fn:escapeXml(hit.lastModifiedBy)}</dd>
                <c:if test="${hit.typeFile}">
                    <dd>size: ${hit.sizeKb}k</dd>
                </c:if>
              </dl>
            </li>
            </s:resultIterator>
          </ol>
        </div>
    </s:results>
    </div>
</center>
</body>
</html>