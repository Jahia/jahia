<%@ page contentType="text/html;charset=UTF-8" language="java"
%><html>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<c:set var="config" value="${functions:default(param.type, 'filepicker')}"/>
<c:set var="xUaCompatible" value="${functions:getInternetExplorerCompatibility(pageContext.request)}"/>
	<head>
        <c:if test="${not empty xUaCompatible}">
            <meta http-equiv="X-UA-Compatible" content="${xUaCompatible}"/>
        </c:if>
        <meta name="robots" content="noindex, nofollow"/>
        <fmt:message key="label.${fn:escapeXml(config)}" var="title"/>
        <title>${fn:escapeXml(title)}</title>
        <internal:gwtGenerateDictionary/>
		<internal:gwtInit locale="${param.lang}" uilocale="${param.uilang}" />
		<internal:gwtImport module="manager" />
        <c:if test="${config == 'filepicker' || config == 'imagepicker'}">
            <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.min.css"/>
            <script type="text/javascript" src="<c:url value='/modules/jquery/javascript/jquery.min.js'/>"></script>
            <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.min.js'/>"></script>
        </c:if>

		<c:if test="${not empty theme}">
			<link rel="stylesheet" type="text/css" href="<c:url value='/engines/${theme}/edit${themeLocale}.css'/>"/>
			<!-- Javascript for theme -->
			<script type="text/javascript" src="<c:url value='/engines/${theme}/js/edit.js'/>"></script>
	    </c:if>

	</head>
	<body>
        <internal:contentPicker conf="${fn:escapeXml(config)}" mimeTypes="${fn:escapeXml(param.mime)}" jahiaServletPath="/cms" filesServletPath="/files" jahiaContextPath="${pageContext.request.contextPath}"  callback="${fn:escapeXml(param.CKEditorFuncNum)}"/>
	</body>
</html>
