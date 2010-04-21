<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="val1" value="${fn:substringBefore(currentNode.properties.facets.string, ';')}"/>
<c:set var="val2" value="${fn:substringAfter(currentNode.properties.facets.string, ';')}"/>

<query:definition var="listQuery" scope="request" >
    <query:selector nodeTypeName="${val1}" selectorName="genericFacets"/>
    <query:column selectorName="genericFacets" columnName="rep:facet()" propertyName="${val2}"/>
</query:definition>
