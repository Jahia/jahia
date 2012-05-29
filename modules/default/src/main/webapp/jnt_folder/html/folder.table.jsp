<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:include view="hidden.header"/>

<table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
    <thead>
    <tr>
        <th width="5%">
            <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentFolder') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
                <a title="parent" href="<c:url value='${url.base}${currentNode.parent.path}.html'/>"><img height="32" width="32"
                                                                                         border="0"
                                                                                         style="cursor: pointer;"
                                                                                         title="parent" alt="parent"
                                                                                         src="<c:url value='${url.currentModule}/images/icons/folder_up.png'/>"></a>
            </c:if>
        </th>
        <th width="25%"><fmt:message key="label.title"/></th>
        <th width="10%" style="white-space: nowrap;"><fmt:message key="label.download"/></th>
        <th width="15%" style="white-space: nowrap;"><fmt:message key="mix_created.jcr_created"/></th>
        <th width="15%" style="white-space: nowrap;"><fmt:message key="mix_lastModified.jcr_lastModified"/></th>
        <th width="15%"><fmt:message key="jmix_lastPublished.j_lastPublished"/></th>
        <th width="15%" style="white-space: nowrap;" class="lastCol"><fmt:message key="label.workflow"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}"
               varStatus="status">
        <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
        <td>${child.properties["jcr:mimeType"].string}

            <jcr:icon var="icon" node="${child}"/>
            <img src="<c:url value='${url.templatesPath}/${icon}_large.png'/>"/>
            <%--<c:if test="${not empty child.primaryNodeType.templatePackage.rootFolder}">--%>
                <%--<jcr:icon var="icon" node="${child}"/>--%>
                <%--<img src="${url.templatesPath}/${icon}_large.png"/>--%>

                <%--&lt;%&ndash;<img src="${url.templatesPath}/${child.primaryNodeType.templatePackage.rootFolder}/icons/${fn:replace(fn:escapeXml(child.primaryNodeType.name),":","_")}_${functions:fileIcon(child.name)}_large.png"/>&ndash;%&gt;--%>
            <%--</c:if>--%>
            <%--<c:if test="${empty child.primaryNodeType.templatePackage.rootFolder}">--%>
				<%--<c:if test="${functions:fileIcon(child.name) != 'file'}">--%>
					<%--<c:set var="icon" value="_${functions:fileIcon(child.name)}"/>--%>
				<%--</c:if>--%>
                <%--<img src="${url.templatesPath}/default/icons/${fn:replace(fn:escapeXml(child.primaryNodeType.name),":","_")}${icon}_large.png"/>--%>
            <%--</c:if>--%>
        </td>
        <td>
            <div class="jahia-template-gxt" jahiatype="module" id="module${child.identifier}" type="existingNode"
                 scriptInfo="" path="${child.path}" template="hidden.system" dragdrop="false">
                <c:if test="${child.locked}">
                    <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Supprimer"
                         src="<c:url value='${url.currentModule}/images/icons/locked.gif'/>">
                </c:if>
                <a href="<c:url value='${url.base}${child.path}.html'/>">
                    ${fn:escapeXml(!empty child.propertiesAsString['jcr:title'] ? child.propertiesAsString['jcr:title'] : child.name)}
                </a>
            </div>
        </td>
		<td><c:if test="${not jcr:isNodeType(child, 'jnt:folder')}"><a href=${child.url}><fmt:message key="label.download"/></a></c:if></td>
        <td>
            <fmt:formatDate value="${child.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td>
            <fmt:formatDate value="${child.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td>
            <fmt:formatDate value="${child.properties['j:lastPublished'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
        </td>
        <td class="lastCol">
            <workflow:activeWorkflow node="${child}" var="wfs"/>
            <c:forEach items="${wfs}" var="wf">
                ${fn:escapeXml(wf.workflowDefinition.displayName)}
            </c:forEach>
        </td>
        </tr>
    </c:forEach>

    <c:if test="${not omitFormatting}">
        <div class="clear"></div>
    </c:if>
    </tbody>
</table>

<template:include view="hidden.footer"/>