<%@ tag body-content="empty" description="Add the theme as a resource" %>

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="theme" value="" />
<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:theme" inherited="true"/>
<c:if test="${not empty theme}">
    <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
        <template:addResources type="css" resources="${themeFile.url}" />
    </c:forEach>
</c:if>

<c:forEach var="tpl" items="${previousTemplate.nextTemplates}">
    <jcr:node uuid="${tpl.node}" var="tplnode" />
    <c:set var="theme" value="" />
    <jcr:nodeProperty var="theme" node="${tplnode}" name="j:theme" />
    <c:if test="${not empty theme && not empty theme.node}">
        <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
            <template:addResources type="css" resources="${themeFile.url}" />
        </c:forEach>
    </c:if>
</c:forEach>