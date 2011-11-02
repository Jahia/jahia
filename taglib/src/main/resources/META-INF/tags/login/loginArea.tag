<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the login controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="doRedirect" required="false" type="java.lang.Boolean"
              description="Do we need to perform a client-side redirect after the login? Setting it to true, will will prevent browser built-in warning message if the page needs to be reloaded (after POST method). [true]"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@tag import="org.jahia.params.valves.LoginEngineAuthValveImpl"%>
<c:set var="org.jahia.tags.login.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:if test="${!currentRequest.logged}">
    <c:set var="formId" value="<%= this.toString() %>"/>
    <c:url value="/cms/login" var="loginUrl"/>
    <c:set target="${attributes}" property="action" value="${functions:default(attributes.action, loginUrl)}"/>
    <c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'loginForm')}"/>
	<c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
    <form ${functions:attributes(attributes)}>
        <c:set var="redirectTo" value="${functions:default(attributes.redirectTo, requestScope['javax.servlet.error.request_uri'])}"/>
        <c:if test="${not empty redirectTo}">
            <input type="hidden" name="redirect" value="${fn:escapeXml(redirectTo)}"/>
        </c:if>
        <c:if test="${empty redirectTo && not empty renderContext && not empty renderContext.mainResource}">
            <input type="hidden" name="redirect" value="<c:url value='${url.base}${renderContext.mainResource.node.path}.html'/>"/>
        </c:if>
        <input type="hidden" name="<%=LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER%>" value="true"/>
        <jsp:doBody/>
    </form>
</c:if>