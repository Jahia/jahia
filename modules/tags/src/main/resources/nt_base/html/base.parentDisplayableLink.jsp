<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<c:choose>
    <c:when test="${jcr:isNodeType(currentNode, 'jmix:hasTemplateNode') && not empty currentNode.properties['j:templateName']}">
        <template:module node="${currentNode}" view="link"/>
    </c:when>
    <c:otherwise>
        <template:module node="${jcr:getParentOfType(currentNode, 'jnt:page')}" view="link"/>
    </c:otherwise>
</c:choose>