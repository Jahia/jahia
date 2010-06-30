<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<item>
    <title><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></title>
    <link>http://localhost:8080${url.base}${currentNode.path}.detail.html</link>
    <description>
        <c:set var="currentList" value="${currentNode.nodes}" scope="request"/>
    <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status">
            <template:module node="${subchild}" template="default" templateType="rss"/>
    </c:forEach>
    </description>
    <pubDate><fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/> <fmt:formatDate
                    value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if></pubDate>
    <dc:date><fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/> <fmt:formatDate
                    value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if></dc:date>
</item>