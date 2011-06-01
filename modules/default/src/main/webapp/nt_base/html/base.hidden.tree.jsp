<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<li id="treeNode${currentNode.identifier}" class="closed">
    <a href="<c:url value='${url.base}${currentNode.path}.${templateForTree}.html'/>"><span class="folder">${currentNode.name}</span></a>
    <c:forEach var="node" items="${jcr:getChildrenOfType(currentNode,nodeTypeForTree)}" varStatus="status">
        <c:if test="${status.first}">
            <ul>
        </c:if>
        <template:module node="${node}" view="hidden.tree" editable="false"/>
        <c:if test="${status.last}">
            </ul>
        </c:if>
    </c:forEach>
</li>