<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


<template:include view="hidden.header"/>
<c:if test="${not renderContext.editMode}">
    <template:addResources type="javascript" resources="jquery.min.js,jquery.bxSlider.js,jquery.bxSlider.load.js"/>
    <template:addResources type="css" resources="jquery.bxSlider.css"/>
</c:if>
<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<div id="example1">

    <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status">
        <c:if test="${jcr:isNodeType(subchild, 'jmix:nodeReference')}">
            <c:choose>
                <c:when test="${renderContext.editMode}">
                    <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                                     editable="${moduleMap.editable}"/>
                </c:when>
                <c:otherwise>
                    <jcr:node var="referedNode" uuid="${subchild.properties['j:node'].string}"/>
                    <c:choose>
                        <c:when test="${jcr:isNodeType(referedNode, 'jmix:thumbnail')}">
                            <div class="item">
                                <c:url value="${referedNode.thumbnailUrls['thumbnail']}" var="imgUrl" />
                                <img src="${imgUrl}" alt=""/><br/>

                                <h3>${referedNode.name}</h3>
                            </div>
                        </c:when>
                        <c:when test="${jcr:isNodeType(referedNode, 'jnt:news')}">
                            <!-- a different rendering if the content is a news -->
                            <div class="item">
                                <jcr:nodeProperty node="${referedNode}" name="image" var="newsImage"/>

                                <img src="${newsImage.node.url}" align="left" alt=""/>

                                <h3><fmt:formatDate value="${referedNode.properties.date.time}"
                                                    pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
                                        value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/><c:if
                                        test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>: <jcr:nodeProperty
                                        node="${referedNode}"
                                        name="jcr:title"/></h3>

                                <p>${referedNode.properties.desc.string}</p>

                            </div>
                        </c:when>
                        <c:when test="${jcr:isNodeType(referedNode, 'jnt:event')}">
                            <!-- a different rendering if the content is an event -->
                            <div class="item">
                                <fmt:message key="label.from"/> <span class="day"><fmt:formatDate pattern="dd"
                                                                                                  value="${referedNode.properties.startDate.time}"/></span>
                                <span class="month"><fmt:formatDate pattern="MM"
                                                                    value="${referedNode.properties.startDate.time}"/></span>
                                <span class="year"><fmt:formatDate pattern="yyyy"
                                                                   value="${referedNode.properties.startDate.time}"/></span>
                                <fmt:message key="label.to"/>
                                <span class="day"><fmt:formatDate pattern="dd"
                                                                  value="${referedNode.properties.endDate.time}"/></span>
                                <span class="month"><fmt:formatDate pattern="MM"
                                                                    value="${referedNode.properties.endDate.time}"/></span>
                                <span class="year"><fmt:formatDate pattern="yyyy"
                                                                   value="${referedNode.properties.endDate.time}"/></span>
                                <h3><jcr:nodeProperty node="${referedNode}" name="jcr:title"/></h3>

                                <p class="eventsLocation"><span>${referedNode.properties.location.string}</span></p>

                                <p class="eventsLocation"><span>${referedNode.properties.eventsType.string}</span></p>

                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="item">
                                <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                                                 editable="${moduleMap.editable}"/>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </c:if>
    </c:forEach>
</div>
<div class="clear"></div>
<c:if test="${moduleMap.editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include view="hidden.footer"/>
