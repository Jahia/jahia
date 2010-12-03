<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="javascript" resources="jquery.min.js"/>

<c:if test="${empty requestScope['org.jahia.modules.newsletter.subscriptionButton']}">
<c:set var="org.jahia.modules.newsletter.subscriptionButton" value="true" scope="request"/>
<template:addResources>
<script type="text/javascript">
function jahiaSubscribe(nodePath) {
	$.ajax({
		type: "POST",
		url: "${url.base}" + nodePath + ".subscribe.do",
		success: function (data, textStatus, xhr) {
			if (data.status == "ok") {
				alert("ok");
			} else if (data.status == "invalid-email") {
		    	<fmt:message key="messsage.subscriptions.invalidEmailAddress" var="msg"/>
		        alert("${functions:escapeJavaScript(msg)}");
			} else if (data.status == 'no-valid-email') {
		    	<fmt:message key="messsage.subscriptions.provideEmailAddress" var="msg"/>
		        alert("${functions:escapeJavaScript(msg)}");
			}
		},
		error: function (xhr, textStatus, errorThrown) {
			if (xhr.status == 401) {
		    	<fmt:message key="label.httpUnauthorized" var="msg"/>
		        alert("${functions:escapeJavaScript(msg)}");
			} else {
				alert(xhr.status + ": " + xhr.statusText);
			}
		},
		dataType: "json"
	});
}
</script> 
</template:addResources>
</c:if>

<c:set var="target" value="${currentNode.properties['j:target'].node}"/>
<c:if test="${not empty target}">
	<p>
		${fn:escapeXml(functions:default(currentNode.propertiesAsString['jcr:title'], target.displayableName))}
	<c:if test="${not renderContext.loggedIn}">
		not logged in
	</c:if>
	<c:if test="${renderContext.loggedIn}">
		&nbsp;<a href="#subscribe" onclick="jahiaSubscribe('${target.path}'); return false;" title="<fmt:message key='label.subscribe'/>"><img src="<c:url value='/icons/jnt_subscriptions.png' context='${url.currentModule}'/>" alt="<fmt:message key='label.subscribe'/>" title="<fmt:message key='label.subscribe'/>" height="16" width="16"/></a>		
	</c:if>
	</p>
</c:if>
<c:if test="${empty target}">
	<p><fmt:message key="label.subscriptions.noTarget"/></p>
</c:if>
