<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources>
    <internal:gwtInit/>
    <link type="text/css" href="${url.context}/gwt/resources/css/gwt-1.4.min.css" rel="stylesheet"/>
</template:addResources>
<script type="text/javascript">
    var portletDeployment =  {
        formActionUrl: "<c:url value='${url.base}${renderContext.mainResource.node.path}.managePortlets.do'/>",
        autoDeploySupported: "true",
        appserverDeployerUrl: "${url.server}${url.context}/manager"
    }
</script>
<internal:gwtGenerateDictionary/>
<internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager" />

<template:gwtJahiaModule id="contentmanager" jahiaType="contentmanager" config="portletdefinitionmanager" embedded="true" />
