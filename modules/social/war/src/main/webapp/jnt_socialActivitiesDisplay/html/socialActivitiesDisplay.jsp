<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>
<jcr:nodeProperty node="${currentNode}" name="j:connectionSource" var="connectionSource"/>
<jcr:nodeProperty node="${currentNode}" name="j:activitiesLimit" var="activitiesLimit"/>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
        <c:if test="${jcr:isNodeType(bindedComponent, 'jnt:user')}">
            <social:get-connections var="connections" path="${bindedComponent.path}"/>
        </c:if>
        <c:if test="${not jcr:isNodeType(bindedComponent, 'jnt:user')}">
            <social:get-acl-connections var="connections" path="${bindedComponent.path}"/>
        </c:if>
        <social:get-activities var="activities" sourcePaths="${connections}" pathFilter="${bindedComponent.path}"/>
        <c:if test="${empty activities}">
            <fmt:message key="message.noActivitiesFound"/>
        </c:if>
        <c:if test="${not empty activities}">
            <div class="boxsocial">
                <div class="boxsocialpadding10 boxsocialmarginbottom16">
                    <div class="boxsocial-inner">
                        <div class="boxsocial-inner-border"><!--start boxsocial -->
                            <ul class="activitiesList">
                                <c:forEach items="${activities}" var="activity">
                                    <template:module path="${activity.path}"/>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
</c:if>
