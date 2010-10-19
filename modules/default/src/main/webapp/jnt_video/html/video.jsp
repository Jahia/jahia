<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<jcr:nodeProperty node="${currentNode}" name="name" var="name"/>
<jcr:nodeProperty node="${currentNode}" name="source" var="source"/>
<jcr:nodeProperty node="${currentNode}" name="width" var="width"/>
<jcr:nodeProperty node="${currentNode}" name="height" var="height"/>
<jcr:nodeProperty node="${currentNode}" name="autostart" var="autostart"/>
<jcr:nodeProperty node="${currentNode}" name="autoplay" var="autoplay"/>
<jcr:nodeProperty node="${currentNode}" name="showcontrols" var="showcontrols"/>
<jcr:node var="video" path="${source.node.properties['j:fullpath'].string}/jcr:content"/>

<object data="${source.node.url}" type="<c:choose>
                                            <c:when test="${video.properties['jcr:mimeType'].string == 'video/avi'}">video/x-msvideo</c:when>
                                            <c:when test="${video.properties['jcr:mimeType'].string == 'video/quicktime'}">video/quicktime</c:when>
                                            <c:when test="${video.properties['jcr:mimeType'].string == 'video/x-ms-wmv'}">video/x-ms-wmv</c:when>
                                            <c:when test="${video.properties['jcr:mimeType'].string == 'video/mpeg'}">video/mpeg</c:when>
                                            <c:otherwise>video/mpeg</c:otherwise>
                                        </c:choose>" width="${width.long}" height="${height.long}">
    <param name="src" value="${source.node.url}">
    <param name="autoplay" value="${autoplay.string}">
    <param name="autoStart" value="${autostart.string}">
    <param name="controller" value="${showcontrols.string}">
    alt : <a href="${source.node.url}">${name.string}</a>
</object>



