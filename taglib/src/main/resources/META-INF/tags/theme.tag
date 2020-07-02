<%@ tag body-content="empty" description="Add the theme as a resource" %>
<%@ attribute name="sortAssetsByNodeName" type="java.lang.Boolean" required="false" rtexprvalue="true"
              description="Do we need to sort the css assets by css file node name? [false]" %>

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="sortAssetsByNodeName" value="${not empty sortAssetsByNodeName ? sortAssetsByNodeName : false}"/>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:themeName" inherited="true"/>
<c:if test="${!empty theme}">
    <jcr:node var="themeFolder" path="${renderContext.site.templatePackage.rootFolderPath}/${renderContext.site.templatePackage.version}/templates/files/themes/${theme.string}"/>
    <c:if test="${!empty themeFolder}">
        <c:set var="themeFiles" value="${jcr:getChildrenOfType(themeFolder,'jnt:file')}"/>
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
    <c:if test="${not empty theme and not empty theme.node}">
        <c:set var="themeFiles" value="${jcr:getChildrenOfType(theme.node,'jnt:file')}"/>
        <c:if test="${sortAssetsByNodeName}">
            <jcr:sort list="${themeFiles}" properties="false,j:nodename,asc" var="themeFiles"/>
        </c:if>
        <c:forEach var="themeFile" items="${themeFiles}">
            <template:addResources type="css" resources="${themeFile.url}"/>
        </c:forEach>
    </c:if>
</c:forEach>