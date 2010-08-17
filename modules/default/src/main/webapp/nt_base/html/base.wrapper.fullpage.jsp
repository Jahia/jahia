<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:template>
	<c:set var="pageTitle" value="${currentNode.properties['jcr:title'].string}"/>
    <template:templateHead title="${fn:escapeXml(not empty pageTitle ? pageTitle : currentNode.name)}">
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<jcr:nodeProperty var="theme" node="${currentNode}" name="j:theme" inherited="true"/>
		<c:if test="${!empty theme}">
			<c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
				<template:addResources type="css" resources="${themeFile.url}" insert="true"/>
			</c:forEach>
		</c:if>
	</template:templateHead>
    <template:templateBody>
        ${wrappedContent}
    </template:templateBody>
</template:template>