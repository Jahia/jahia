<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="depthLimit" value="${functions:default(currentResource.moduleParams.depthLimit, 1)}"/>
<c:set var="nodeTypes" value="${functions:default(currentResource.moduleParams.nodeTypes, param.nodeTypes)}"/>
<c:set var="selectableNodeTypes" value="${functions:default(currentResource.moduleParams.selectableNodeTypes, param.selectableNodeTypes)}"/>
<json:object>
    <c:forEach items="${currentNode.properties}" var="prop">
        <c:choose>
            <c:when test="${not prop.definition.multiple}">
                <json:property name="${prop.name}" value="${prop.string}"/>
            </c:when>
            <c:otherwise>
                <json:array name="${prop.name}" items="${prop.values}" var="propValue">${propValue.string}</json:array>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <json:property name="path" value="${currentNode.path}" />
    <json:property name="identifier" value="${currentNode.identifier}" />
    <json:property name="index" value="${currentNode.index}" />
    <json:property name="depth" value="${currentNode.depth}" />
    <json:property name="nodename" value="${currentNode.name}" />
    <json:property name="primaryNodeType" value="${currentNode.primaryNodeType.name}" />
    <c:if test="${jcr:isNodeType(currentNode, 'mix:title')}">
    <jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
    </c:if>
    <json:property name="text" value="${not empty title ? title.string : currentNode.name}"/>
    <c:if test="${(empty selectableNodeTypes || jcr:isNodeType(currentNode, selectableNodeTypes)) and (empty param.displayablenodeonly or (param.displayablenodeonly eq 'true' and jcr:isDisplayable(currentNode, renderContext)))}">
        <json:property name="classes" value="selectable"/>
    </c:if>
    <json:property name="hasChildren" value="${not empty nodeTypes ? jcr:hasChildrenOfType(currentNode, nodeTypes) : currentNode.nodes.size > 0}"/>
    <c:if test="${depthLimit > 0}">
        <c:if test="${not empty nodeTypes ? jcr:hasChildrenOfType(currentNode, nodeTypes) : currentNode.nodes.size > 0}">
            <json:array name="childNodes">
                <c:forEach items="${currentNode.nodes}" var="child">
                    <template:module node="${child}" templateType="json" editable="false" view="full">
                        <template:param name="depthLimit" value="${depthLimit -1 }" />
                    </template:module>
                </c:forEach>
            </json:array>
        </c:if>
    </c:if>
</json:object>