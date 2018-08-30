<%@ page contentType="text/html;charset=UTF-8" language="java"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="xUaCompatible" value="${functions:getInternetExplorerCompatibility(pageContext.request)}"/>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <c:if test="${not empty xUaCompatible}">
        <meta http-equiv="X-UA-Compatible" content="${xUaCompatible}"/>
    </c:if>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="author" content="system" />
    <title>Edit</title>

    <internal:gwtGenerateDictionary/>
    <internal:gwtInit />
    <internal:gwtImport module="edit"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.min.css"/>
    <script type="text/javascript" src="<c:url value='/modules/jquery/javascript/jquery.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/clippy/jquery.clippy.min.js'/>"></script>

	<c:if test="${not empty theme}">

		<%-- load theme --%>
        <c:choose>
            <c:when test="${renderContext.editModeConfigName == 'studiomode'}">
                <script type="text/javascript">
                    <%-- hack for studio only, this will force the switchConfig action items to be open in a new HTTP call --%>
                    window.jahiaReplaceSwitchConfigByOpen = true;
                </script>
            </c:when>
            <c:otherwise>
                <link rel="stylesheet" type="text/css" media="screen" href="<c:url value='/engines/${theme}/edit${themeLocale}.css'/>" />
                <script type="text/javascript" src="<c:url value='/engines/${theme}/js/edit.js'/>"></script>
            </c:otherwise>
        </c:choose>
    </c:if>
</head>

<body ${theme != null ? "class=\"theme-"+theme+"\"" : ""}>
    <div class="jahia-template-gxt editmode-gxt" jahiatype="editmode" id="editmode" config="${renderContext.editModeConfigName}" path="${currentResource.node.path}" locale="${currentResource.locale}" template="${currentResource.template}" nodetypes="${fn:replace(jcr:getConstraints(renderContext.mainResource.node),',',' ')}"></div>
</body>

</html>
