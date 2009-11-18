<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<c:set value="true" var="editable" scope="request" />
<c:choose>
<c:when test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">
  <jcr:jqom var="sortedChildren">
    <query:selector nodeTypeName="nt:base" selectorName="children"/>
    <query:childNode selectorName="children" path="${currentNode.realNode.path}"/>
    <c:forTokens var="prefix" items="first,second,third" delims=",">
      <jcr:nodeProperty node="${currentNode}" name="${prefix}Field" var="sortPropertyName"/>
      <c:if test="${!empty sortPropertyName}">
        <jcr:nodeProperty node="${currentNode}" name="${prefix}Direction" var="order"/>
        <query:sortBy propertyName="${sortPropertyName.string}" order="${order.string}" selectorName="children"/>
      </c:if>
    </c:forTokens>
  </jcr:jqom>
  <c:set value="${sortedChildren.nodes}" var="currentList" scope="request"/>  
</c:when>
<c:otherwise>
  <c:set value="${currentNode.editableChildren}" var="currentList" scope="request"/>
</c:otherwise>
</c:choose>