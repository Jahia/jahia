<%@ tag body-content="scriptless" dynamic-attributes="attributes" description="Renders the HTML form element with required hidden input fields to perform search drill down request." %>
<%@ attribute name="excludeParamsRegex" required="false" type="java.lang.String" description="A regular expression to exclude matching reuqest parameters if needed." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:if test="${empty attributes.action}">
	<c:url var="actionUrl" value="${url.base}${renderContext.mainResource.node.path}.html"/>
</c:if>
<c:set target="${attributes}" property="action" value="${functions:default(attributes.action, actionUrl)}"/>
<c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'searchDrillDownForm')}"/>
<c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
<form ${functions:attributes(attributes)}>
    <input type="hidden" name="jcrMethodToCall" value="get" />
    <c:forEach items="${param}" var="par">
        <c:if test="${fn:startsWith(par.key, 'src_')}">
            <c:if test="${excludeParamsRegex == null || !functions:matches(excludeParamsRegex, par.key)}">
                <c:set var="paramKey" value="${fn:escapeXml(par.key)}"/>
                <c:forEach items="${paramValues[par.key]}" var="value">
                    <input type="hidden" name="${paramKey}" value="${fn:escapeXml(value)}"/>
                </c:forEach>
            </c:if>
        </c:if>
    </c:forEach>
    <jsp:doBody/>
</form>