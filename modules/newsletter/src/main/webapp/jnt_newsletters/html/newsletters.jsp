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

<template:include view="hidden.header"/>
<p>
    <img src="<c:url value='${url.currentModule}/images/jahiapp-newsletter-small.png'/>"/>
</p>
<table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
    <thead>
    <tr>
        <th width="5%">&nbsp;</th>
        <th width="60%"><fmt:message key="label.title"/></th>
        <th width="15%" style="white-space: nowrap; text-align: center;"><fmt:message key="mix_created.jcr_created"/></th>
        <th width="15%" style="white-space: nowrap; text-align: center;"><fmt:message key="mix_lastModified.jcr_lastModified"/></th>
        <th width="5%" style="white-space: nowrap; text-align: center;"><fmt:message key="jnt_newsletters.issueCount"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${moduleMap.currentList}" var="newsletter" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
        <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
        <td align="center">
            <img src="<c:url value='${url.currentModule}/icons/jnt_newsletter_large.png'/>" height="48" width="48" alt=" "/>
        </td>
        <td>
            <div class="jahia-template-gxt" jahiatype="module" id="newsletter-${newsletter.identifier}" type="existingNode"
                 scriptInfo="" path="${newsletter.path}" template="hidden.system" dragdrop="false">
                <a href="<c:url value='${url.base}${newsletter.path}.html'/>">
                    ${fn:escapeXml(!empty newsletter.propertiesAsString['jcr:title'] ? newsletter.propertiesAsString['jcr:title'] : newsletter.name)}
				</a>
            </div>
        </td>
        <td align="center">
            <fmt:formatDate value="${newsletter.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td align="center">
            <fmt:formatDate value="${newsletter.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td align="center">
            ${functions:length(jcr:getChildrenOfType(newsletter, 'jnt:newsletterIssue'))}
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

<template:include view="hidden.footer"/>
