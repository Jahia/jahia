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
<%@page import="java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="url" class="java.lang.String" scope="request" />
<% // http files path. %>
<jsp:useBean id="input" class="java.lang.String" scope="request" />
<% // inputs size. %>
<jsp:useBean id="values" class="java.util.HashMap" scope="request" />
<% // Map containing values. %>
<jsp:useBean id="operation" class="java.lang.String" scope="request" />
<% // The selected operation. %>
<c:choose>
<c:when test="${indexExists == false}">
<p class="errorBold">
    <fmt:message key="org.jahia.admin.search.ManageSearch.indexNotExist.label" />
</p>
</c:when>
<c:otherwise>
<jsp:include page="processing.jsp" flush="true" />
</c:otherwise>
</c:choose>
<div class="head headtop">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.search.ManageSearch.availableOperation.label" />
    </div>
</div>
<c:choose>
<c:when test="${indexingDisabled}">
  <fmt:message key="org.jahia.admin.search.ManageSearch.indexingDisabled.label"/>
</c:when>
<c:when test="${localIndexing == false}">
  <fmt:message key="org.jahia.admin.search.ManageSearch.noLocalIndexing.label"/>
</c:when>
<c:otherwise>
  <ul style="list-style-type: none">
    <li>
        <input type="radio" name="operation" value="doindex"<% if (operation.equals("doindex")) { %> checked<%} %>><fmt:message key="org.jahia.admin.search.ManageSearch.reIndexAndOptimize.label"/>
    </li>
  </ul>
</c:otherwise>
</c:choose>