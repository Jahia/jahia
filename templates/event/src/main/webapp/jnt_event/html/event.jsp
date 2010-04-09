<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="event.css"/>


        <div class="eventsListItem"><!--start eventsListItem -->
            <div class="eventsInfoDate">
                <div class="eventsDate">
                    <span class="day"><fmt:formatDate pattern="dd" value="${currentNode.properties.startDate.time}"/></span>
                    <span class="month"><fmt:formatDate pattern="MM" value="${currentNode.properties.startDate.time}"/></span>
                    <span class="year"><fmt:formatDate pattern="yyyy" value="${currentNode.properties.startDate.time}"/></span>
                </div>
                <c:if test="${not empty currentNode.properties.endDate}">
                    <div class="eventsTxtDate">
                        <span><fmt:message key='to'/></span>
                    </div>
                    <div class="eventsDate">
                        <span class="day"><fmt:formatDate pattern="dd" value="${currentNode.properties.endDate.time}"/></span>
                        <span class="month"><fmt:formatDate pattern="MM" value="${currentNode.properties.endDate.time}"/></span>
                        <span class="year"><fmt:formatDate pattern="yyyy" value="${currentNode.properties.endDate.time}"/></span>
                    </div>
                </c:if>
            </div>
            <div class="eventsBody"><!--start eventsBody -->
                <p class="eventsLocation"><span>${currentNode.properties.location.string}</span></p>
                <p class="eventsType"><span>${currentNode.properties.eventsType.string}</span></p>
                <h4><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h4>

                <div class="eventsResume">
                    ${currentNode.properties.body.string}</div>
                    <div class="eventsMeta">
        	    <span class="categoryLabel"><fmt:message key='category'/>:</span>
                        <jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="cat"/>
                        <c:if test="${cat != null}">
                                    <c:forEach items="${cat}" var="category">
                                        <span class="categorytitle">${category.category.title}</span>
                                    </c:forEach>

                        </c:if>
                    </div>
            </div>
            <!--start eventsBody -->
            <div class="clear"> </div>
        </div>