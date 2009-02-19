<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

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
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the search controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search"%>
<c:set var="display" value="${h:default(display, true)}"/>
<%@ attribute name="searchFor" required="false" type="java.lang.String"
              description="Specifies the search mode: pages or files [pages]." %>
<%@ attribute name="resultsPage" required="false" type="java.lang.String"
              description="You can set the target JSP template to be used after form submit. By default the JSP page is used, which is configured in the search-results element of the template deployment descriptor (templates.xml) for the current template set. The special keyword - this - can be used to identify the current page (the page will be preserved after form submit)." %>
<jsp:useBean id="searchTermIndexes" class="java.util.HashMap" scope="request"/>
<c:set var="formId" value="<%= this.toString() %>"/>
<c:set target="${searchTermIndexes}" property="${formId}" value="0"/>
<c:set target="${attributes}" property="action" value="${h:default(attributes.action, jahia.page.url)}"/>
<c:set target="${attributes}" property="name" value="${h:default(attributes.name, 'searchForm')}"/>
<c:set target="${attributes}" property="method" value="${h:default(attributes.method, 'get')}"/>
<c:set var="searchFor" value="${h:default(searchFor, 'autodetect')}"/>
<c:if test="${empty resultsPage}">
    <s:resultsPageUrl var="resultsPage"/>
</c:if>
<form ${h:attributes(attributes)}>
    <input type="hidden" name="src_mode" value="${searchFor}"/>
    <c:if test="${not empty resultsPage && resultsPage != 'this'}">
        <input type="hidden" name="template" value="${resultsPage}"/>
    </c:if>
    <jsp:doBody/>
</form>