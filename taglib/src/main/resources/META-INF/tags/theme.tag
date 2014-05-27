<%@ tag body-content="empty" description="Add the theme as a resource" %>
<%@ attribute name="sortAssetsByNodeName" type="java.lang.Boolean" required="false" rtexprvalue="true"
              description="Do we need to sort the css assets by css file node name? [false]" %>

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="sortAssetsByNodeName" value="${functions:default(sortAssetsByNodeName, false)}"/>

<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:theme" inherited="true"/>
<c:if test="${!empty theme}">
    <c:if test="${!empty theme}">
        <c:set var="themeFiles" value="${jcr:getChildrenOfType(theme.node,'jnt:file')}"/>
        <c:if test="${sortAssetsByNodeName}">
            <jcr:sort list="${themeFiles}" properties="false,j:nodename,asc" var="themeFiles" />
        </c:if>
        <c:forEach var="themeFile" items="${themeFiles}">
            <template:addResources type="css" resources="${themeFile.url}" />
        </c:forEach>
    </c:if>
</c:if>

<c:forEach var="tpl" items="${previousTemplate.nextTemplates}">
    <jcr:node uuid="${tpl.node}" var="tplnode" />
    <jcr:nodeProperty var="theme" node="${tplnode}" name="j:theme" />
    <c:if test="${not empty theme.node}">
        <c:set var="themeFiles" value="${jcr:getChildrenOfType(theme.node,'jnt:file')}"/>
        <c:if test="${sortAssetsByNodeName}">
            <jcr:sort list="${themeFiles}" properties="false,j:nodename,asc" var="themeFiles" />
        </c:if>
        <c:forEach var="themeFile" items="${themeFiles}">
            <template:addResources type="css" resources="${themeFile.url}" />
        </c:forEach>
    </c:if>
</c:forEach>