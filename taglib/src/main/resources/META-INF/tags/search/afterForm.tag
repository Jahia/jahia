<%@ attribute name="excludeParamsRegex" required="false" type="java.lang.String"%>
<%@ attribute name="cssClass" required="false" type="java.lang.String"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:url var="actionUrl" value="${url.base}${renderContext.mainResource.node.path}.html"/>
<c:if test="${empty cssClass}">
    <c:set var="classAttr" value=""/>
</c:if>
<c:if test="${not empty cssClass}">
    <c:set var="classAttr" value="class=&quot;${cssClass}&quot;"/>
</c:if>
<form method="post" action="${actionUrl}" ${classAttr}>
    <c:forEach items="${param}" var="par">
        <c:if test="${fn:startsWith(par.key, 'src_')}">
            <c:if test="${excludeParamsRegex == null || !functions:matches(excludeParamsRegex, par.key)}">
                <c:forEach items="${par.value}" var="value">
                    <input type="hidden" name="${par.key}" value="${fn:escapeXml(value)}"/>
                </c:forEach>
            </c:if>
        </c:if>
    </c:forEach>
    <input type="hidden" name="jcrMethodToCall" value="get"/>
    <jsp:doBody/>
</form>