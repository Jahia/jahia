<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<c:set var="cfg" value="${functions:default(param.conf, 'repositoryexplorer')}"/>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=8"/>
    <meta name="robots" content="noindex, nofollow"/>
    <fmt:message key="label.${fn:escapeXml(cfg)}" var="title"/>
    <title>${fn:escapeXml(title)}</title>
    <internal:gwtGenerateDictionary/>
    <internal:gwtInit/>
    <script type="text/javascript">
        var contextJsParameters=jahiaGWTParameters;
    </script>
    <internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager"/>
    <c:if test="${cfg == 'filemanager' || cfg == 'repositoryexplorer' || cfg == 'editorialcontentmanager'}">
        <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.css"/>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.min.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.min.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/clippy/jquery.clippy.min.js'/>"></script>
    </c:if>
</head>
<body onload="window.focus()">
<internal:contentManager conf="${fn:escapeXml(cfg)}" selectedPaths="${fn:escapeXml(param.selectedPaths)}"/>
</body>
</html>