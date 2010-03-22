<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:if test="${currentNode.nodes.size > 0}">
    <c:set value="${fn:randomInt(currentNode.nodes.size)}" var="itemToDisplay"/>
    <c:forEach items="${currentNode.children}" var="subchild" begin="${itemToDisplay}" end="${itemToDisplay}">
        <template:module node="${subchild}" forcedTemplate="${subNodesTemplate}" editable="true"/>
    </c:forEach>
</c:if>
<c:if test="${renderContext.editMode}">
    <c:if test="${currentNode.nodes.size <= 0}">
        <template:module path="*" />
    </c:if>
</c:if>