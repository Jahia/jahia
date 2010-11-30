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

<template:include template="hidden.header"/>

<table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
    <thead>
    <tr>
        <th width="5%">&nbsp;</th>
        <th width="35%"><fmt:message key="label.title"/></th>
        <th width="13%" style="white-space: nowrap; text-align: center;"><fmt:message key="mix_created.jcr_created"/></th>
        <th width="15%" style="white-space: nowrap; text-align: center;"><fmt:message key="mix_lastModified.jcr_lastModified"/></th>
        <th width="12%" style="white-space: nowrap; text-align: center;"><fmt:message key="jnt_newsletterIssue.j_lastSent"/></th>
        <th width="15%" style="white-space: nowrap; text-align: center;"><fmt:message key="jnt_newsletterIssue.j_scheduled"/></th>
        <th width="5%">&nbsp;</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${moduleMap.currentList}" var="issue" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
        <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
        <td align="center">
        	<c:url value="/icons/jnt_newsletterIssue${empty issue.properties['j:scheduled'] ? (empty issue.properties['j:lastSent'] ? '' : 'Sent') : 'Scheduled'}_large.png" context="${url.currentModule}" var="statusImage"/>
            <img src="${statusImage}" height="48" width="48" alt=" "/>
        </td>
        <td>
            <div class="jahia-template-gxt" jahiatype="module" id="newsletter-${issue.identifier}" type="existingNode"
                 scriptInfo="" path="${issue.path}" template="hidden.system" dragdrop="false">
                <a href="<c:url context='${url.base}' value='${issue.path}.html'/>">
                    ${fn:escapeXml(!empty issue.propertiesAsString['jcr:title'] ? issue.propertiesAsString['jcr:title'] : issue.name)}
				</a>
            </div>
        </td>
        <td align="center">
            <fmt:formatDate value="${issue.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td align="center">
            <fmt:formatDate value="${issue.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td align="center">
            <fmt:formatDate value="${issue.properties['j:lastSent'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td align="center">
            <div class="jahia-template-gxt" jahiatype="module" id="newsletter-${issue.identifier}-schedule" type="existingNode"
                 scriptInfo="" path="${issue.path}" template="hidden.system" dragdrop="false">
				<c:choose>
					<c:when test="${not empty issue.properties['j:scheduled']}">
            			<fmt:formatDate value="${issue.properties['j:scheduled'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            		</c:when>
            		<c:otherwise>&nbsp;</c:otherwise>
            	</c:choose>
            </div>
        </td>
        <td align="center">
        	<img src="<c:url value='/icons/jnt_newsletterIssueSent.png' context='${url.currentModule}'/>" height="24" width="24" alt=" "/>
        </td>
        </tr>
    </c:forEach>

    <c:if test="${not omitFormatting}">
        <div class="clear"></div>
    </c:if>
    </tbody>
</table>

<c:if test="${moduleMap.editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>

<template:include template="hidden.footer"/>

<c:if test="${renderContext.editMode}">
<p>
	<a href="#subscription-manager" onclick="jahia.openSubscriptionManager('${currentNode.identifier}'); return false;"><img src="<c:url value='/icons/subscriptionManager.png' context='${url.currentModule}'/>" height="48" width="48" alt=" " align="middle"/></a>&nbsp;
	<a href="#subscription-manager" onclick="jahia.openSubscriptionManager('${currentNode.identifier}'); return false;"><fmt:message key="label.subscriptionManager"/></a>
</p>
</c:if>