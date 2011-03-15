<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<c:set var="cookieName" value="rated${currentNode.identifier}"/>
<c:if test="${renderContext.loggedIn and (empty cookie[cookieName])}">
    <form action="<c:url value='${url.base}${currentNode.path}'/>" method="post"
          id="jahia-forum-post-vote-${currentNode.identifier}">
        <input type="hidden" name="redirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
            <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="methodToCall" value="put"/>
        <input type="hidden" name="j:lastVote" value="1"/>
        <input type="hidden" name="cookieValue" value="${currentNode.identifier}"/>
        <input type="hidden" name="cookieName" value="${cookieName}"/>
    </form>
</c:if>        