<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:if test="${not empty title}">
    <c:set var="title" value="${title.string}"/>
</c:if>
<c:if test="${empty title}">
    <c:set var="title" value="${currentNode.name}"/>
</c:if>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<item>
    <title>${fn:escapeXml(title)}</title>
    <link><c:url value="${url.server}${url.base}${currentNode.path}.html"/></link>
    <description>${functions:abbreviate(functions:removeHtmlTags(currentNode.properties.desc.string),200,250,'...')}</description>
    <pubDate><fmt:formatDate value="${created.date.time}" type="both" dateStyle="full" timeStyle="long"/></pubDate>
</item>