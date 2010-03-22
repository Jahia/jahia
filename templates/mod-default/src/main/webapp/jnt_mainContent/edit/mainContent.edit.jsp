<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<div class="maincontent">
    <h3 class="title edit${currentNode.identifier}" jcr:id="jcr:title" jcr:url="${url.base}${currentNode.path}">
        <jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
    <c:if test="${!empty image}">
        <div class="imagefloat${currentNode.properties.align.string} file${currentNode.identifier}" jcr:id="image"
             jcr:url="${url.base}${currentNode.path}">
            <img src="${image.node.url}" alt="${image.node.url}"/>
        </div>
    </c:if>
    <c:if test="${empty image}">
        <div class="imagefloat${currentNode.properties.align.string} file${currentNode.identifier}" jcr:id="image"
             jcr:url="${url.base}${currentNode.path}">
            <span>click here to attach a file</span>
        </div>
    </c:if>
    <jcr:propertyInitializers var="options" nodeType="jnt:mainContent" name="align"/>
    <span jcr:id="align" class="choicelistEdit${currentNode.identifier}"
                              jcr:url="${url.base}${currentNode.path}"
                              jcr:options="{<c:forEach items="${options}" varStatus="status" var="option"><c:if test="${status.index > 0}">,</c:if>'${option.value.string}':'${option.displayName}'</c:forEach>}">Image Alignment${currentNode.properties.align.string}</span>
        <span jcr:id="body" class="ckeditorEdit${currentNode.identifier}"
              id="ckeditorEdit${currentNode.identifier}${scriptPropName}"
              jcr:url="${url.base}${currentNode.path}">${currentNode.properties.body.string}</span>
</div>
<br class="clear"/>