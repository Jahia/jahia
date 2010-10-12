<%@ tag body-content="empty" description="Add the theme as a resource" %>

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:theme" inherited="true"/>
<c:if test="${!empty theme}">
    <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
        <template:addResources type="css" resources="${themeFile.url}" insert="true"/>
    </c:forEach>
</c:if>

<c:forEach var="tpl" items="${previousTemplate.nextTemplates}">
    <jcr:nodeProperty var="theme" node="${tpl.node}" name="j:theme" />
    <c:if test="${!empty theme}">
        <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
            <template:addResources type="css" resources="${themeFile.url}" insert="true"/>
        </c:forEach>
    </c:if>
</c:forEach>