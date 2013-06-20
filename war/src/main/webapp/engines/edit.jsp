<%@ page import="org.jahia.services.content.nodetypes.ConstraintsHelper" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="author" content="system" />
    <title>Edit</title>

    <internal:gwtGenerateDictionary/>
    <internal:gwtInit />
    <script type="text/javascript">
        var contextJsParameters=jahiaGWTParameters;
    </script>
    <internal:gwtImport module="org.jahia.ajax.gwt.module.edit.Edit"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.min.css"/>
    <script type="text/javascript" src="<c:url value='/modules/jquery/javascript/jquery.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/clippy/jquery.clippy.min.js'/>"></script>
</head>

<body>
    <div class="jahia-template-gxt editmode-gxt" jahiatype="editmode" id="editmode" config="${renderContext.editModeConfigName}" path="${currentResource.node.path}" locale="${currentResource.locale}" template="${currentResource.template}" nodetypes="${fn:replace(jcr:getConstraints(renderContext.mainResource.node),',',' ')}"></div>
</body>

</html>
