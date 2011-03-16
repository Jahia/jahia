<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="news.css"/>


 <jcr:nodeProperty node="${currentNode}" name="date" var="newsDate"/>
 <jcr:nodeProperty node="${currentNode}" name="desc" var="newsDesc"/>
 <jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>

<div class="newsListItem"><!--start newsListItem -->
    <h4><a href="<c:url value='${url.base}${currentNode.path}.detail.html'/>"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a></h4>

    <p class="newsInfo">
        <span class="newsLabelDate"><fmt:message key="label.date"/> :</span>
            <span class="newsDate">
                <fmt:formatDate value="${newsDate.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
                    value="${newsDate.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
            </span>
    </p>
    <c:url value="${url.files}${newsImage.node.path}" var="imageUrl"/>
    <div class="newsImg"><a href="<c:url value='${url.base}${currentNode.path}.detail.html'/>"><img src="${imageUrl}"/></a>
    </div>
    <div class="newsResume">
        ${newsDesc.string}
    </div>

    <div class="more"><span><a href="<c:url value='${url.base}${currentNode.path}.detail.html'/>">
        <fmt:message key="label.read"/>: <jcr:nodeProperty node="${currentNode}" name="jcr:title"/>
    </a></span></div>
    <div class="clear"></div>
</div>