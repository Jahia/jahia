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


<template:include view="hidden.load"/>

<template:addResources type="javascript" resources="jquery.js,jquery.bxSlider.js,jquery.bxSlider.load.js"/>
<template:addResources type="css" resources="jquery.bxSlider.css"/>

<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<!-- r�cup�rer une liste d'items de type teasers et boucler dessus -->


<div id="example1">

    <c:forEach items="${moduleMap.currentList}" var="child" varStatus="status">
        <jcr:node var="child" uuid="${child.properties['j:node'].string}"/>
        <c:if test="${jcr:isNodeType(child, 'jmix:thumbnail')}">
            <div class="item">

                <img src="${url.context}/repository/default${child.path}/thumbnail" alt=""><br/>

                <h3>${child.name}</h3>

                <p> ${currentNode.properties.abstract.string} </p>
            </div>
        </c:if>

        <!-- a different rendering if the content is a news -->
        <c:if test="${jcr:isNodeType(child, 'jnt:news')}">
            <div class="item">
                <jcr:nodeProperty node="${child}" name="image" var="newsImage"/>

                <img src="${newsImage.node.url}" align="left"/>

                <h3><fmt:formatDate value="${child.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
                        value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/><c:if
                        test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>: <jcr:nodeProperty node="${child}"
                                                                                                    name="jcr:title"/></h3>

                <p>${child.properties.desc.string}</p>

            </div>
        </c:if>

        <!-- a different rendering if the content is an event -->

        <c:if test="${jcr:isNodeType(child, 'jnt:event')}">
            <div class="item">
                <fmt:message key="label.from"/> <span class="day"><fmt:formatDate pattern="dd" value="${child.properties.startDate.time}"/></span>
                <span class="month"><fmt:formatDate pattern="MM" value="${child.properties.startDate.time}"/></span>
                <span class="year"><fmt:formatDate pattern="yyyy" value="${child.properties.startDate.time}"/></span>
                <fmt:message key="label.to"/>
                <span class="day"><fmt:formatDate pattern="dd" value="${child.properties.endDate.time}"/></span>
                <span class="month"><fmt:formatDate pattern="MM" value="${child.properties.endDate.time}"/></span>
                <span class="year"><fmt:formatDate pattern="yyyy" value="${child.properties.endDate.time}"/></span>


                <h3><jcr:nodeProperty node="${child}" name="jcr:title"/></h3>

                <p class="eventsLocation"><span>${child.properties.location.string}</span></p>

                <p class="eventsLocation"><span>${child.properties.eventsType.string}</span></p>

            </div>
        </c:if>



    </c:forEach>

    <template:module path="*"/>

</div>
