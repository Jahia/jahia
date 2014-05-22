<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<c:set var="node" value="${reference.node}"/>
<c:choose>
    <c:when test="${not empty node}">
        <template:addCacheDependency uuid="${currentNode.properties['j:node'].string}"/>
        <template:module node="${currentNode.contextualizedNode}" editable="false" view="${currentNode.properties['j:referenceView'].string}">
            <template:param name="refTitle" value="${currentNode.properties['jcr:title'].string}"/>
        </template:module>
    </c:when>
    <c:otherwise>
        <fmt:message key="label.missingReference"/>
    </c:otherwise>
</c:choose>        