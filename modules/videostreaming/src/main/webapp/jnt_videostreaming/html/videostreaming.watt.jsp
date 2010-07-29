<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<object type="application/x-shockwave-flash" data="http://www.wat.tv/swf2/${currentNode.properties.identifier.string}" width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}">
	<param name="allowFullScreen" value="${currentNode.properties.allowfullscreen.boolean}" />
	<param name="allowScriptAccess" value="always" />
	<param name="movie" value="http://www.wat.tv/swf2/${currentNode.properties.identifier.string}" />
	<embed width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}" src="http://www.wat.tv/swf2/${currentNode.properties.identifier.string}"  allowscriptaccess="always" allowfullscreen="${currentNode.properties.allowfullscreen.boolean}" />
</object>