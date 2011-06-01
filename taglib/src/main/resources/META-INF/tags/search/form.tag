<%@ tag body-content="scriptless" description="Renders the HTML form element to wrap up the search controls." %>
<%@ tag dynamic-attributes="attributes"%>
<jsp:useBean id="searchTermIndexes" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="searchTermMatchIndexes" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="searchTermFieldIndexes" class="java.util.HashMap" scope="request"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search"%>
<c:set var="org.jahia.tags.search.form.formId" value="<%= this.toString() %>" scope="request"/>
<c:set var="formId" value="<%= this.toString() %>"/>
<c:set target="${searchTermIndexes}" property="${formId}" value="0"/>
<c:set target="${searchTermMatchIndexes}" property="${formId}" value="0"/>
<c:set target="${searchTermFieldIndexes}" property="${formId}" value="0"/>
<c:if test="${empty attributes.action}">
	<c:url var="actionUrl" value="${url.base}${renderContext.mainResource.node.path}.search.html"/>
</c:if>
<c:set target="${attributes}" property="action" value="${functions:default(attributes.action, actionUrl)}"/>
<c:set target="${attributes}" property="name" value="${functions:default(attributes.name, 'searchForm')}"/>
<c:set target="${attributes}" property="method" value="${functions:default(attributes.method, 'post')}"/>
<form ${functions:attributes(attributes)}>
    <input type="hidden" name="jcrMethodToCall" value="get" />
    <input type="hidden" name="src_originSiteKey" value="${renderContext.site.siteKey}"/>
    <jsp:doBody/>
</form>