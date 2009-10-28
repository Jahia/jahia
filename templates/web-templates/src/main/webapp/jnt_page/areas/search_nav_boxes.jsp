<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<jcr:node var="rootPage" path="/content/sites/${renderContext.site.siteKey}/home" />
<template:module path="${rootPage.path}/sideMenu" >
    <template:import>
        <sideMenu xmlns:j='http://www.jahia.org/jahia/1.0' xmlns:jcr='http://www.jcp.org/jcr/1.0' 
                    jcr:primaryType='jnt:navBar' jcr:mixinTypes='jmix:renderable' j:maxDepth='3' j:nodename='jnt_navBar' j:startLevel='2' j:template='sideMenu'/>
    </template:import>
</template:module>

<template:module path="columnB_box" template="default" autoCreateType="jnt:contentList" />