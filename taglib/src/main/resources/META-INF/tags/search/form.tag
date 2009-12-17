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
<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the search controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="resultsPage" required="false" type="java.lang.String"
              description="You can set the target JSP template to be used after form submit. By default the JSP page is used, which is configured in the search-results element of the template deployment descriptor (templates.xml) for the current template set. The special keyword - this - can be used to identify the current page (the page will be preserved after form submit)." %>
<jsp:useBean id="searchTermIndexes" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="searchTermMatchIndexes" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="searchTermFieldIndexes" class="java.util.HashMap" scope="request"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search"%>
<c:set var="org.jahia.tags.search.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:set var="formId" value="<%= this.toString() %>"/>
<c:set target="${searchTermIndexes}" property="${formId}" value="0"/>
<c:set target="${searchTermMatchIndexes}" property="${formId}" value="0"/>
<c:set target="${searchTermFieldIndexes}" property="${formId}" value="0"/>
<c:set target="${attributes}" property="action" value="${functions:default(attributes.action, url.mainResource)}"/>
<c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'searchForm')}"/>
<c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
<c:set var="searchFor" value="${functions:default(searchFor, 'pages')}"/>
<c:if test="${empty resultsPage}">
    <s:resultsPageUrl var="resultsPage"/>
</c:if>
<form ${functions:attributes(attributes)}>
    <c:if test="${not empty resultsPage && resultsPage != 'this'}">
        <input type="hidden" name="template" value="${fn:escapeXml(resultsPage)}"/>
    </c:if>
    <jsp:doBody/>
</form>