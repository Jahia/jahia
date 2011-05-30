<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:set var="descriptorUrl" value="${url.server}${url.templateTypes['xml']}"/>
<c:set var="title" value="${functions:default(currentNode.propertiesAsString['jcr:title'], 'Jahia search')}"/>
<template:addResources>
<link rel="search" type="application/opensearchdescription+xml" href="${fn:escapeXml(descriptorUrl)}" title="${fn:escapeXml(title)}" />
</template:addResources>
<c:if test="${empty requestScope['org.jahia.modules.search.addOpenSearch']}">
    <template:addResources>
        <script type="text/javascript">
            function addOpenSearch(provider) {
                if ((typeof window.external == "object") && ((typeof window.external.AddSearchProvider == "unknown") || (typeof window.external.AddSearchProvider == "function"))) {
                    window.external.AddSearchProvider(provider);
                } else {
                    alert("You will need a browser which supports OpenSearch to install this plugin.");
                }
            }
        </script>
    </template:addResources>
    <c:set var="org.jahia.modules.search.addOpenSearch" value="true" scope="request"/>
</c:if>
<a href="#opensearch" onclick="addOpenSearch('${descriptorUrl}'); return false;" title="${fn:escapeXml(title)}"><img src="<c:url value='${url.currentModule}/icons/jnt_openSearchDescriptor.png'/>" height="16" width="16" alt=" "/>&nbsp;${fn:escapeXml(title)}</a>