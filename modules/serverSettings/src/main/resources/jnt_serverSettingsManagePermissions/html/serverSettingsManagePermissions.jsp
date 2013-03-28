<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<template:addResources>
    <internal:gwtInit/>
    <link type="text/css" href="${url.context}/gwt/resources/css/gwt-1.4.min.css" rel="stylesheet"/>
</template:addResources>
<internal:gwtGenerateDictionary/>
<internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager" />

<template:gwtJahiaModule id="contentmanager" jahiaType="contentmanager" config="rolesmanager" embedded="true" />
