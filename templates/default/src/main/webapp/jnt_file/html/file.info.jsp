<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="wf" type="org.jahia.services.workflow.WorkflowDefinition"--%>
<tr class="evenLine">
    <td align="center">
   </td>
    <td >
        ${fn:escapeXml(currentNode.fileContent.contentType)}
    </td>
    <td> <a href="${currentNode.url}"><c:if test="${!empty currentNode.properties['jcr:title'].string}">
        ${fn:escapeXml(currentNode.properties['jcr:title'].string)}
    </c:if>
        <c:if test="${empty currentNode.properties['jcr:title'].string}">
        ${fn:escapeXml(currentNode.name)}
    </c:if>
        </a>
    </td>
    <td>
        <fmt:formatDate value="${currentNode.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td>
    <td>
        <fmt:formatDate value="${currentNode.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td align="center">
    <td>
        <fmt:formatDate value="${currentNode.properties['j:lastPublished'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td>
    <td>
        <workflow:activeWorkflow node="${currentNode}" var="wfs"/>
        <c:forEach items="${wfs}" var="wf">
                ${wf.id}  
        </c:forEach>
    </td>
    <td class="lastCol">
    <c:if test="${currentNode.locked}">
        <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Supprimer" src="${url.currentModule}/images/icons/locked.gif">
    </c:if>
    </td>
</tr>