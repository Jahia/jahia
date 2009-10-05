<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>

<jcr:nodeProperty node="${currentNode}" name="url" var="url"/>
<jcr:nodeProperty node="${currentNode}" name="entriesCount" var="entriesCount"/>

<template:cache cacheKey="rssOutput" expiration="60">
    <c:import var="xml" url="http://www.theserverside.com/rss/theserverside-rss2.xml"/>
    <x:parse var="rss" xml="${xml}"/>
    <ul>
        <x:forEach select="$rss//channel/item" var="n" begin="0" end="${entriesCount.long}">

            <li>
                <a href="<x:out select="$n/link"/>">
                    <x:out select="$n/title"/>
                </a><br/>
                <span><x:out select="$n/description" escapeXml="false"/></span>
            </li>
            <c:if test="${nb == entriesCount.long}"><</c:if>
        </x:forEach>
    </ul>
    <span> Rss updated on : <fmt:formatDate value="${now}" dateStyle="full" type="both"/><br/></span>
</template:cache>
