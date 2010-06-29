<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="css" resources="metrics.css"/>
<jcr:nodeProperty node="${currentNode}" name="j:nodeTypeFilter" var="nodeTypeFilter"/>
<jcr:nodeProperty node="${currentNode}" name="j:recommendationLimit" var="recommendationLimit"/>
<c:set var="bindedComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">

<div class="metrics">
<c:catch var="ex">
    <sql:setDataSource driver="com.mysql.jdbc.Driver" url="jdbc:mysql://127.0.0.1:3306/jahia-6.5" user="jahia"
                       password="jahia" var="ds"/>

    <c:if test="${not empty ex and renderContext.editMode}">
        SELECT * FROM objxref WHERE leftpath='${bindedComponent.path}' and rightnodetype='${nodeTypeFilter.string}' order by counter desc limit ${recommendationLimit.long};
    </c:if>

    <sql:query var="refs" dataSource="${ds}">
        SELECT * FROM objxref WHERE leftpath='${bindedComponent.path}' and rightnodetype='${nodeTypeFilter.string}' order by counter desc limit ${recommendationLimit.long};
    </sql:query>
    <p><!--fmt:message key="recommendationsIntro"/-->Users that have viewed this content have also viewed :</p>
    <ol>
    <c:forEach items="${refs.rows}" var="ref">
        <jcr:node path="${ref.rightpath}" var="curNode"/>
        <li><a href="${url.base}${ref.rightpath}"><jcr:nodeProperty node="${curNode}" name="jcr:title"/> (${ref.counter})</a></li>
    </c:forEach>
    </ol>


</c:catch>
<c:if test="${not empty ex and renderContext.editMode}">
    <c:if test = "${ex!=null}">
    The exception is : ${ex}<br><br>
    There is an exception: ${ex.message}<br>
    </c:if>
    <p>For this module to work you need to parse metrics logs of jahia</p>
</c:if>
</div>
</c:if>
<template:linker property="j:bindedComponent"/>