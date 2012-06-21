<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%-- You can use a depthLimit query parameter to set a different depth limit (default = 1) --%>
<%-- You can use a escapeColom query parameter to activate or deactivate JCR property name escaping (replacing colon with underscore --%>
<c:set target="${renderContext}" property="contentType" value="application/json;charset=UTF-8"/>
<c:set var="depthLimit" value="${functions:default(param.depthLimit, 1)}" />
<c:set var="depthLimit" value="${functions:default(currentResource.moduleParams.depthLimit, depthLimit)}"/>
<c:set var="nodeTypes" value="${functions:default(currentResource.moduleParams.nodeTypes, param.nodeTypes)}"/>
<c:set var="selectableNodeTypes" value="${functions:default(currentResource.moduleParams.selectableNodeTypes, param.selectableNodeTypes)}"/>
<c:set var="escapeColon" value="${functions:default(param.escapeColon, false)}" />
<c:set var="withType" value="${functions:default(param.withType, false)}" />
<c:set var="prettyPrint" value="${functions:default(param.prettyPrint, false)}" />
<json:object escapeXml="false" prettyPrint="${prettyPrint}">
    <c:forEach items="${currentNode.properties}" var="prop">
        <c:set var="propName" value="${prop.name}"/>
        <c:if test="${escapeColon}">
            <c:set var="propName" value="${fn:replace(prop.name, ':', '_')}"/>
        </c:if>
        <c:choose>
            <c:when test="${not prop.definition.multiple}">
                <json:property name="${propName}" value="${prop.string}"/>
            </c:when>
            <c:otherwise>
                <json:array name="${propName}" items="${prop.values}" var="propValue">${propValue.string}</json:array>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <json:property name="path" value="${currentNode.path}" />
    <json:property name="identifier" value="${currentNode.identifier}" />
    <json:property name="index" value="${currentNode.index}" />
    <json:property name="depth" value="${currentNode.depth}" />
    <json:property name="nodename" value="${currentNode.name}" />
    <json:property name="primaryNodeType" value="${currentNode.primaryNodeType.name}" />
    <json:array name="mixinTypes" items="${currentNode.mixinNodeTypes}" var="mixinType">
        ${mixinType.name}
    </json:array>
    <json:array name="supertypes" items="${currentNode.primaryNodeType.supertypes}" var="supertype">
        ${supertype.name}
    </json:array>
    <json:property  name="parentPath" value="${currentNode.parent.path}" />
    <json:property name="parentPrimaryNodeType" value="${currentNode.parent.primaryNodeType.name}" />
    <json:array name="parentMixinTypes" items="${currentNode.parent.mixinNodeTypes}" var="parentMixinType">
        ${parentMixinType.name}
    </json:array>
    <json:array name="parentSupertypes" items="${currentNode.parent.primaryNodeType.supertypes}" var="parentSupertype">
        ${parentSupertype.name}
    </json:array>

    <json:property name="depthLimit" value="${depthLimit}" />
    <c:if test="${jcr:isNodeType(currentNode, 'mix:title')}">
    <jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
    </c:if>
    <json:property name="text" value="${not empty title ? title.string : currentNode.name}"/>
    <c:if test="${(empty selectableNodeTypes || jcr:isNodeType(currentNode, selectableNodeTypes)) and (empty param.displayablenodeonly or (param.displayablenodeonly eq 'true' and jcr:isDisplayable(currentNode, renderContext)))}">
        <json:property name="classes" value="selectable"/>
    </c:if>
    <json:property name="hasChildren" value="${not empty nodeTypes ? jcr:hasChildrenOfType(currentNode, nodeTypes) : currentNode.nodes.size > 0}"/>
    <c:if test="${withType}">
        <template:module node="${currentNode}" templateType="json" editable="false" view="nodetype">
            <template:param name="typePropertyName" value="nodeType" />
        </template:module>
    </c:if>
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