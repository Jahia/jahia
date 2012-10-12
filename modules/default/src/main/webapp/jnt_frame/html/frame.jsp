<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<c:if test="${renderContext.editMode}">
  <!-- Leaves an area to edit the frame component -->
  <br/><br/>
</c:if> 
<iframe name="${currentNode.properties.name.string}"  src="${currentNode.properties.source.string}" width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}" scrolling="${currentNode.properties.scrolling.string}" frameborder="${currentNode.properties.frameborder.boolean ? '1' : '0'}" marginheight="${currentNode.properties.marginheight.long}" marginwidth="${currentNode.properties.marginwidth.long}">
  <p>Your browser does not support iframes.</p>
</iframe>