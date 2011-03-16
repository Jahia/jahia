<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="news.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="newsTitle"/>
<jcr:nodeProperty node="${currentNode}" name="date" var="newsDate"/>
<jcr:nodeProperty node="${currentNode}" name="desc" var="newsDesc"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>

<jcr:nodeProperty node="${currentNode}" var="newsCategories" name="j:defaultCategory"/>
<div class="newsItem"><!--start newsItem -->
    <h2>${newsTitle.string}</h2>

    <p class="newsInfo">
        <span class="newsLabelDate"><fmt:message key="label.date"/>:</span>
            <span class="newsDate"><fmt:formatDate value="${newsDate.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
                    value="${newsDate.date.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if></span>
    </p>
    <!-- display categories applied on this news -->
    <c:if test="${!empty newsCategories }">
        <div class="newsMeta">
            <span class="categoryLabel"><fmt:message key='label.categories'/> :</span>
            <jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="cat"/>
            <c:if test="${cat != null}">
                        <c:forEach items="${cat}" var="category">
                            <span class="categorytitle">${category.node.properties['j:nodename'].string}</span>
                        </c:forEach>

            </c:if>
        </div>
    </c:if>
    <!-- image and news body -->
    <c:url value="${url.files}${newsImage.node.path}" var="imageUrl"/>
    <div class="newsImg"><img src="${imageUrl}"/></div>

    <div class="newsText">
        ${newsDesc.string}
    </div>
    <a class="returnLink" href="<c:url value='${url.base}${jcr:getParentOfType(currentNode, "jnt:page").path}.html'/>" title='<fmt:message key="backToNewsList"/>'><fmt:message key='backToNewsList'/></a>

    <div class="clear"></div>
</div>