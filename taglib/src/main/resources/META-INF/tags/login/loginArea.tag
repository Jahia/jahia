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
<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the login controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="doRedirect" required="false" type="java.lang.Boolean"
              description="Do we need to perform a client-side redirect after the login? Setting it to true, will will prevent browser built-in warning message if the page needs to be reloaded (after POST method). [true]"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@tag import="org.jahia.params.valves.LoginEngineAuthValveImpl"%>
<c:set var="org.jahia.tags.login.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:if test="${!currentRequest.logged}">
  <c:set var="formId" value="<%= this.toString() %>"/>
  <c:set target="${attributes}" property="action" value="${h:default(attributes.action, jahia.page.url)}"/>
  <c:set target="${attributes}" property="name" value="${h:default(attributes.name, 'loginForm')}"/>
  <c:set target="${attributes}" property="method" value="${h:default(attributes.method, 'post')}"/>
  <form ${h:attributes(attributes)}>
    <input type="hidden" name="<%=LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER%>" value="1"/>
    <c:if test="${doRedirect}">
        <input type="hidden" name="<%=LoginEngineAuthValveImpl.DO_REDIRECT%>" value="true"/>
    </c:if>
    <jsp:doBody/>
  </form>
</c:if>