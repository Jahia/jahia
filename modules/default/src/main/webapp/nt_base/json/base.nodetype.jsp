<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%-- You can use a withHidden query parameter to generate output of hidden properties and child definitions --%>
<%-- You can use a withProtected query parameter to generate output of protected properties and child definitions --%>
<%-- You can use a nodeType query parameter to generate output of a specific node type (not related to the node) --%>
<%-- You can use a depthLimit query parameter to control the depth of recursive output of the child definition node types --%>
<c:set target="${renderContext}" property="contentType" value="application/json;charset=UTF-8"/>
<c:set var="withHidden" value="${functions:default(param.withHidden, false)}" />
<c:set var="withProtected" value="${functions:default(param.withProtected, false)}" />
<c:set var="nodeTypeName" value="${functions:default(currentResource.moduleParams.nodeType, param.nodeType)}"/>
<c:set var="nodeType" value="${currentNode.primaryNodeType}" />
<c:set var="depthLimit" value="${functions:default(param.depthLimit, 1)}" />
<c:set var="depthLimit" value="${functions:default(currentResource.moduleParams.depthLimit, depthLimit)}"/>
<c:if test="${not empty nodeTypeName}">
    <jcr:nodeType name="${nodeTypeName}" var="nodeType" />
</c:if>
<c:set var="prettyPrint" value="${functions:default(param.prettyPrint, false)}" />
<json:object name="${currentResource.moduleParams.typePropertyName}" escapeXml="false" prettyPrint="${prettyPrint}">
    <json:object name="primaryNodeType">
        <c:set var="primaryNodeType" value="${nodeType}" />
        <json:property name="name" value="${primaryNodeType.name}" />
        <%-- Property definitions --%>
        <json:array name="propertyDefinitions">
            <c:forEach items="${primaryNodeType.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${((propertyDefinition.hidden && withHidden) || !(propertyDefinition.hidden)) && ((propertyDefinition.protected && withProtected) || !(propertyDefinition.protected))}">
                    <json:object>
                        <json:property name="name" value="${propertyDefinition.name}" />
                        <json:property name="type" value="${jcr:propertyTypeName(propertyDefinition.requiredType)}" />
                        <json:property name="multiple" value="${propertyDefinition.multiple}" />
                        <c:if test="${withHidden}">
                            <json:property name="hidden" value="${propertyDefinition.hidden}" />
                        </c:if>
                        <c:if test="${withProtected}">
                            <json:property name="protected" value="${propertyDefinition.protected}" />
                        </c:if>
                        <json:property name="index" value="${propertyDefinition.index}" />
                        <json:property name="internationalized" value="${propertyDefinition.internationalized}" />
                        <json:array name="defaultValues" items="${propertyDefinition.defaultValues}" var="defaultValue">
                            ${defaultValue.string}
                        </json:array>
                    </json:object>
                </c:if>
            </c:forEach>
        </json:array>
        <json:array name="supertypes" items="${primaryNodeType.supertypes}" var="supertype">
            ${supertype.name}
        </json:array>
        <json:array name="subtypes" items="${primaryNodeType.subtypesAsList}" var="subtype">
            ${subtype.name}
        </json:array>
        <%-- Child definitions --%>
        <json:array name="childDefinitions">
            <c:forEach items="${primaryNodeType.childNodeDefinitions}" var="childDefinition">
                <c:if test="${((childDefinition.hidden && withHidden) || !(childDefinition.hidden)) && ((childDefinition.protected && withProtected) || !(childDefinition.protected))}">
                    <json:object>
                        <json:property name="name" value="${childDefinition.name}" />
                        <json:property name="defaultPrimaryTypeName" value="${childDefinition.defaultPrimaryTypeName}" />
                        <json:property name="mandatory" value="${childDefinition.mandatory}" />
                        <json:array name="requiredPrimaryTypeNames" items="${childDefinition.requiredPrimaryTypeNames}" />
                        <json:property name="prefix" value="${childDefinition.prefix}" />
                        <c:if test="${withHidden}">
                            <json:property name="hidden" value="${childDefinition.hidden}" />
                        </c:if>
                        <c:if test="${withProtected}">
                            <json:property name="protected" value="${childDefinition.protected}" />
                        </c:if>
                        <c:if test="${depthLimit > 0}">
                            <c:forEach items="${childDefinition.requiredPrimaryTypeNames}" var="requiredPrimaryTypeName">
                                <template:module node="${currentNode}" templateType="json" editable="false" view="nodetype">
                                    <template:param name="depthLimit" value="${depthLimit -1 }" />
                                    <template:param name="nodeType" value="${requiredPrimaryTypeName}" />
                                    <template:param name="typePropertyName" value="nodeType" />
                                </template:module>
                            </c:forEach>
                        </c:if>
                    </json:object>
                </c:if>
            </c:forEach>
        </json:array>
    </json:object>
    <c:if test="${empty nodeTypeName}">
        <json:array name="mixinTypes" items="${currentNode.mixinNodeTypes}" var="mixinNodeType">
            <json:object>
                <json:property name="name" value="${mixinNodeType.name}" />
                <%-- Property definitions --%>
                <json:array name="propertyDefinitions">
                    <c:forEach items="${mixinNodeType.propertyDefinitions}" var="propertyDefinition">
                        <c:if test="${((propertyDefinition.hidden && withHidden) || !(propertyDefinition.hidden)) && ((propertyDefinition.protected && withProtected) || !(propertyDefinition.protected))}">
                            <json:object>
                                <json:property name="name" value="${propertyDefinition.name}" />
                                <json:property name="type" value="${jcr:propertyTypeName(propertyDefinition.requiredType)}" />
                                <json:property name="multiple" value="${propertyDefinition.multiple}" />
                                <c:if test="${withHidden}">
                                    <json:property name="hidden" value="${propertyDefinition.hidden}" />
                                </c:if>
                                <c:if test="${withProtected}">
                                    <json:property name="protected" value="${propertyDefinition.protected}" />
                                </c:if>
                                <json:property name="index" value="${propertyDefinition.index}" />
                                <json:property name="internationalized" value="${propertyDefinition.internationalized}" />
                                <json:array name="defaultValues" items="${propertyDefinition.defaultValues}" var="defaultValue">
                                    ${defaultValue.string}
                                </json:array>
                            </json:object>
                        </c:if>
                    </c:forEach>
                </json:array>
                <%-- Child definitions --%>
                <json:array name="childDefinitions">
                    <c:forEach items="${mixinNodeType.childNodeDefinitions}" var="childDefinition">
                        <c:if test="${((childDefinition.hidden && withHidden) || !(childDefinition.hidden)) && ((childDefinition.protected && withProtected) || !(childDefinition.protected))}">
                            <json:object>
                                <json:property name="name" value="${childDefinition.name}" />
                                <json:property name="defaultPrimaryTypeName" value="${childDefinition.defaultPrimaryTypeName}" />
                                <json:property name="mandatory" value="${childDefinition.mandatory}" />
                                <json:array name="requiredPrimaryTypeNames" items="${childDefinition.requiredPrimaryTypeNames}" />
                                <json:property name="prefix" value="${childDefinition.prefix}" />
                                <c:if test="${withHidden}">
                                    <json:property name="hidden" value="${childDefinition.hidden}" />
                                </c:if>
                                <c:if test="${withProtected}">
                                    <json:property name="protected" value="${childDefinition.protected}" />
                                </c:if>
                                <c:if test="${depthLimit > 0}">
                                    <c:forEach items="${childDefinition.requiredPrimaryTypeNames}" var="requiredPrimaryTypeName">
                                        <template:module node="${currentNode}" templateType="json" editable="false" view="nodetype">
                                            <template:param name="depthLimit" value="${depthLimit -1 }" />
                                            <template:param name="nodeType" value="${requiredPrimaryTypeName}" />
                                            <template:param name="typePropertyName" value="nodeType" />
                                        </template:module>
                                    </c:forEach>
                                </c:if>
                            </json:object>
                        </c:if>
                    </c:forEach>
                </json:array>

            </json:object>
        </json:array>
    </c:if>
</json:object>