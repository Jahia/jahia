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
<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the search controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="searchFor" required="false" type="java.lang.String"
              description="Specifies the search mode: pages or files. [pages]" %>
<%@ attribute name="resultsPage" required="false" type="java.lang.String"
              description="You can set the target JSP template to be used after form submit. By default the JSP page is used, which is configured in the search-results element of the template deployment descriptor (templates.xml) for the current template set. The special keyword - this - can be used to identify the current page (the page will be preserved after form submit)." %>
<jsp:useBean id="searchTermIndexes" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="searchTermMatchIndexes" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="searchTermFieldIndexes" class="java.util.HashMap" scope="request"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search"%>
<c:set var="org.jahia.tags.search.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="formId" value="<%= this.toString() %>"/>
<c:set target="${searchTermIndexes}" property="${formId}" value="0"/>
<c:set target="${searchTermMatchIndexes}" property="${formId}" value="0"/>
<c:set target="${searchTermFieldIndexes}" property="${formId}" value="0"/>
<c:set target="${attributes}" property="action" value="${h:default(attributes.action, jahia.page.url)}"/>
<c:set target="${attributes}" property="name" value="${h:default(attributes.name, 'searchForm')}"/>
<c:set target="${attributes}" property="method" value="${h:default(attributes.method, 'post')}"/>
<c:set var="searchFor" value="${h:default(searchFor, 'pages')}"/>
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