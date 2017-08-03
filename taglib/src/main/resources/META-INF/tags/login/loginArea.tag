<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the login controls." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="doRedirect" required="false" type="java.lang.Boolean"
              description="Do we need to perform a client-side redirect after the login? Setting it to true, will will prevent browser built-in warning message if the page needs to be reloaded (after POST method). [true]"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="org.jahia.tags.login.form.class" value="<%= this.getClass() %>" scope="request"/>
<c:if test="${!currentRequest.logged}">
    <c:set var="formId" value="<%= this.toString() %>"/>
    <c:url value="/cms/login" var="loginUrl"/>
    <c:set target="${attributes}" property="action" value="${functions:default(attributes.action, loginUrl)}"/>
    <c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'loginForm')}"/>
	<c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
    <form ${functions:attributes(attributes)}>
        <input type="hidden" name="site" value="${functions:currentSiteKey(pageContext.request)}"/>
        <c:choose>
            <c:when test="${not empty attributes.redirectTo}">
                <input type="hidden" name="redirect" value="${fn:escapeXml(attributes.redirectTo)}"/>
            </c:when>
            <c:when test="${not empty requestScope['javax.servlet.error.request_uri']}">
                <c:choose>
                    <c:when test="${fn:startsWith(requestScope['javax.servlet.error.request_uri'], '//')}">
                        <c:set var="redirectUrl" value=""/>
                        <c:forEach items="${fn:split(requestScope['javax.servlet.error.request_uri'], '/')}" var="path">
                            <c:if test="${!empty path}">
                                <c:set var="redirectUrl" value="${redirectUrl}/${path}"/>
                            </c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <c:set var="redirectUrl" value="${requestScope['javax.servlet.error.request_uri']}"/>
                    </c:otherwise>
                </c:choose>
                <c:url var="redirect" value="${redirectUrl}" context="/">
                    <c:forEach items="${paramValues}" var="paramValueEntry">
                        <c:forEach items="${paramValueEntry.value}" var="paramValue">
                            <c:if test="${paramValueEntry.key != 'username' && paramValueEntry.key != 'password' && paramValueEntry.key != 'redirect'}">
                                <c:param name="${paramValueEntry.key}" value="${paramValue}"/>
                            </c:if>    
                        </c:forEach>
                    </c:forEach>
                </c:url>
                <input type="hidden" name="redirect" value="${fn:escapeXml(redirect)}"/>
            </c:when>
            <c:when test="${not empty renderContext && not empty renderContext.mainResource}">
                <c:choose>
                    <c:when test="${renderContext.mainResource.template != 'default'}">
                        <c:set var="urlEnd" value=".${renderContext.mainResource.template}.html"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="urlEnd" value=".html"/>
                    </c:otherwise>
                </c:choose>
                <c:url var="redirect" value="${url.base}${renderContext.mainResource.node.path}${urlEnd}">
                    <c:forEach items="${paramValues}" var="paramValueEntry">
                        <c:forEach items="${paramValueEntry.value}" var="paramValue">
                            <c:if test="${paramValueEntry.key != 'username' && paramValueEntry.key != 'password' && paramValueEntry.key != 'redirect'}">
                                <c:param name="${paramValueEntry.key}" value="${paramValue}"/>
                            </c:if>   
                        </c:forEach>
                    </c:forEach>
                </c:url>
                <input type="hidden" name="redirect" value="${fn:escapeXml(redirect)}"/>
                <input type="hidden" name="failureRedirect" value="${fn:escapeXml(redirect)}"/>
            </c:when>
        </c:choose>
        <jsp:doBody/>
    </form>
</c:if>