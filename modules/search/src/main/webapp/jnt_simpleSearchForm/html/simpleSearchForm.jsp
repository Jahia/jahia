<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="simplesearchform.css" />

<template:addCacheDependency uuid="${currentNode.properties.result.string}"/>
<c:if test="${not empty currentNode.properties.result.node}">
<c:url value='${url.base}${currentNode.properties.result.node.path}.html' var="searchUrl"/>
<s:form method="post" class="simplesearchform" action="${searchUrl}">
		<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
		<c:if test="${not empty title.string}">
		<label for="searchTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
		</c:if>
		<fmt:message key='search.startSearching' var="startSearching"/>
       	<s:term match="all_words" id="searchTerm" value="${startSearching}" searchIn="siteContent,tags" onfocus="if(this.value=='${startSearching}')this.value='';" onblur="if(this.value=='')this.value='${startSearching}';" class="text-input"/>
       	<s:site value="${renderContext.site.name}" includeReferencesFrom="systemsite" display="false"/>
       	<s:language value="${renderContext.mainResource.locale}" display="false" />
    	<input class="searchsubmit" type="submit"  title="<fmt:message key='search.submit'/>" value=""/>

</s:form><br class="clear"/>
</c:if>