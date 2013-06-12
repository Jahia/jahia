<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<template:addResources>
    <link type="text/css" href="<c:url value='/gwt/resources/css/gwt-1.4.min.css'/>" rel="stylesheet"/>
</template:addResources>
<internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager" />

<template:gwtJahiaModule id="contentmanager" jahiaType="contentmanager" config="rolesmanager" embedded="true" />
