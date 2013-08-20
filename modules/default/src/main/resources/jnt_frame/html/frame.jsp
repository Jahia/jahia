<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>

<c:if test="${renderContext.editMode}">
  <!-- Leaves an area to edit the frame component -->
  <br/><br/>
</c:if>
<c:set var="scrolling" value="${fn:escapeXml(currentNode.properties.scrolling.string)}" />
<c:set var="frameborder" value="${currentNode.properties.frameborder.boolean}" />
<c:set var="marginheight" value="${currentNode.properties.marginheight.long}" />
<c:if test="${empty marginheight}"><c:set var="marginheight" value="0" /></c:if>
<c:set var="marginwidth" value="${currentNode.properties.marginwidth.long}" />
<c:if test="${empty marginwidth}"><c:set var="marginwidth" value="0" /></c:if>
<iframe title="${fn:escapeXml(currentNode.properties['jcr:title'].string)}"
        name="${fn:escapeXml(currentNode.properties.name.string)}"
        src="${fn:escapeXml(currentNode.properties.source.string)}"
        width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}"
        scrolling="${scrolling}"<%-- HTML 4 --%>
        frameborder="${frameborder ? '1' : '0'}"<%-- HTML 4 --%>
        marginheight="${marginheight}"<%-- HTML 4 --%>
        marginwidth="${marginwidth}"<%-- HTML 4 --%>
        style="overflow:${scrolling eq 'yes' ? 'scroll' : scrolling eq 'no' ? 'hidden' : 'auto'};border:${frameborder ? 'medium solid black' : '0 none'};margin:${marginheight}px ${marginwidth}px"><%-- HTML 5 --%>
  <p>Your browser does not support iframes.</p>
</iframe>