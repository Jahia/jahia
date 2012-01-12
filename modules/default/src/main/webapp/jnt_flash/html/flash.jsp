<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jcr:nodeProperty node="${currentNode}" name="flashSource" var="flashSource"/>
<jcr:nodeProperty node="${currentNode}" name="width" var="widthFlash"/>
<jcr:nodeProperty node="${currentNode}" name="height" var="heightFlash"/>
<jcr:nodeProperty node="${currentNode}" name="flashPlayer" var="flashPlayer"/>
<jcr:nodeProperty node="${currentNode}" name="id" var="idFlash"/>
<jcr:nodeProperty node="${currentNode}" name="name" var="nameFlash"/>
<jcr:nodeProperty node="${currentNode}" name="swliveconnect" var="swliveconnectFlash"/>
<jcr:nodeProperty node="${currentNode}" name="play" var="playFlash"/>
<jcr:nodeProperty node="${currentNode}" name="loop" var="loopFlash"/>
<jcr:nodeProperty node="${currentNode}" name="menu" var="menuFlash"/>
<jcr:nodeProperty node="${currentNode}" name="quality" var="qualityFlash"/>
<jcr:nodeProperty node="${currentNode}" name="scale" var="scaleFlash"/>
<jcr:nodeProperty node="${currentNode}" name="align" var="alignFlash"/>
<jcr:nodeProperty node="${currentNode}" name="salign" var="salignFlash"/>
<jcr:nodeProperty node="${currentNode}" name="wmode" var="wmodeFlash"/>
<jcr:nodeProperty node="${currentNode}" name="bgcolor" var="bgcolorFlash"/>
<jcr:nodeProperty node="${currentNode}" name="base" var="baseFlash"/>
<jcr:nodeProperty node="${currentNode}" name="flashvars" var="flashvarsFlash"/>

<template:addResources type="javascript" resources="swfobject.js"/>
<template:addResources type="css" resources="flash.css"/>

<script type="text/javascript">
    swfobject.registerObject("flashcontent${currentNode.UUID}", "${flashPlayer.string}");
</script>
<object id="flashcontent${currentNode.UUID}" width="${widthFlash.string}" height="${heightFlash.string}"
        data="${flashSource.node.url}">

    <c:if test="${not empty wmodeFlash.string}">
        <param name="wmode" value="${wmodeFlash}"/>
    </c:if>
    <c:if test="${not empty idFlash.string}">
        <param name="id" value="${idFlash.string}"/>
    </c:if>
    <c:if test="${not empty swliveconnectFlash.string}">
        <param name="swliveconnect" value="${swliveconnectFlash.string}"/>
    </c:if>
    <c:if test="${not empty wmodeFlash.string}">
        <param name="wmode" value="${wmodeFlash.string}"/>
    </c:if>
    <c:if test="${not empty loopFlash.string}">
        <param name="loop" value="${loopFlash.string}"/>
    </c:if>
    <c:if test="${not empty playFlash.string}">
        <param name="play" value="${playFlash.string}"/>
    </c:if>
    <c:if test="${not empty menuFlash.string}">
        <param name="menu" value="${menuFlash.string}"/>
    </c:if>
    <c:if test="${not empty scaleFlash.string}">
        <param name="scale" value="${scaleFlash.string}"/>
    </c:if>
    <c:if test="${not empty qualityFlash.string}">
        <param name="quality" value="${qualityFlash.string}"/>
    </c:if>
    <c:if test="${not empty alignFlash.string}">
        <param name="align" value="${alignFlash.string}"/>
    </c:if>
    <c:if test="${not empty salignFlash.string}">
        <param name="salign" value="${salignFlash.string}"/>
    </c:if>
    <c:if test="${not empty baseFlash.string}">
        <param name="base" value="${baseFlash.string}"/>
    </c:if>
    <c:if test="${not empty flashvarsFlash.string}">
        <param name="flashvars" value="${flashvarsFlash.string}"/>
    </c:if>
    <!--[if !IE]>-->
    <object type="application/x-shockwave-flash" data="${flashSource.node.url}" width="${widthFlash.string}"
            height="${heightFlash.string}">
        <!--<![endif]-->
        <div class="flashcontent">
            <strong><fmt:message key="label.flashplayer.info"/></strong><br/>
            <a href="http://www.adobe.com/go/getflashplayer" target="_blank"><img
                    src="<c:url value='${url.currentModule}/images/160x41_Get_Flash_Player.jpg'/>"
                    alt="get flash player"/></a>
        </div>
        <!--[if !IE]>-->
    </object>
    <!--<![endif]-->
</object>