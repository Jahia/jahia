<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="rss.css" />

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="url" var="feedUrl"/>
<jcr:nodeProperty node="${currentNode}" name="nbEntries" var="nbEntries"/>
<jsp:useBean id="rss" class="org.jahia.modules.rss.RSSUtil"/>
<c:set target="${rss}" property="url" value="${feedUrl.string}"/>
<%-- load the feed using RSSUtil --%>
<c:set var="syndFeed" value="${rss.feed}"/>
<c:if test="${not empty title && not empty title.string}">
    <h3 class="titlerss">
        <img title="" alt="" src="<c:url value='${url.currentModule}/images/rss.png'/>"/> ${fn:escapeXml(title.string)}
    </h3>
</c:if>
<c:if test="${empty syndFeed}">
    <jcr:nodeProperty node="${currentNode}" name="url" var="feedUrl"/>
    <fmt:message key="jnt_rss.rssLoadError"><fmt:param value="${feedUrl.string}"/></fmt:message>
</c:if>
<c:if test="${not empty syndFeed}">
    <div class="syndFeed">
        <div class="syndFeedTitle">
            <a href="${fn:escapeXml(syndFeed.link)}">${fn:escapeXml(syndFeed.title)}</a>
            <c:if test="${not empty syndFeed.image}">
                  <img src="${fn:escapeXml(syndFeed.image.url)}" title="${fn:escapeXml(syndFeed.image.title)}" alt="${fn:escapeXml(syndFeed.image.description)}"/>
            </c:if>
        </div>
        <div class="syndFeedEntries">
            <c:forEach items="${syndFeed.entries}" var="syndEntry" begin="0" end="${nbEntries.long - 1}">
                <div class="syndEntryTitle">
                    <a href="${fn:escapeXml(syndEntry.link)}">${fn:escapeXml(syndEntry.title)}&nbsp;[${fn:escapeXml(syndEntry.updatedDate)}]</a>
                </div>
                <div class="syndEntryDescription">
                    ${syndEntry.description.value}
                </div>
                <div class="syndEntryContents">
                    <c:forEach items="${syndEntry.contents}" var="syndContent">
                        <div class="syndEntryContent">
                        	${syndContent.value}
                        </div>
                    </c:forEach>
                </div>
                <div class="syndEntryAuthor">
                        ${fn:escapeXml(syndEntry.author)}
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>